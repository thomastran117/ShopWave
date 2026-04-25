package backend.services.impl;

import backend.models.core.MarketplaceVendor;
import backend.models.core.VendorSLABreach;
import backend.models.core.VendorSLAMetric;
import backend.models.core.VendorSLAPolicy;
import backend.models.enums.SLABreachAction;
import backend.models.enums.VendorStatus;
import backend.repositories.MarketplaceVendorRepository;
import backend.repositories.VendorAnalyticsRepository;
import backend.repositories.VendorSLABreachRepository;
import backend.repositories.VendorSLAMetricRepository;
import backend.repositories.VendorSLAPolicyRepository;
import backend.repositories.projections.VendorShipHoursProjection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class VendorSLAEvaluationScheduler {

    private static final Logger log = LoggerFactory.getLogger(VendorSLAEvaluationScheduler.class);

    private final VendorSLAPolicyRepository policyRepository;
    private final VendorSLAMetricRepository metricRepository;
    private final VendorSLABreachRepository breachRepository;
    private final VendorAnalyticsRepository analyticsRepository;
    private final MarketplaceVendorRepository marketplaceVendorRepository;

    public VendorSLAEvaluationScheduler(
            VendorSLAPolicyRepository policyRepository,
            VendorSLAMetricRepository metricRepository,
            VendorSLABreachRepository breachRepository,
            VendorAnalyticsRepository analyticsRepository,
            MarketplaceVendorRepository marketplaceVendorRepository) {
        this.policyRepository = policyRepository;
        this.metricRepository = metricRepository;
        this.breachRepository = breachRepository;
        this.analyticsRepository = analyticsRepository;
        this.marketplaceVendorRepository = marketplaceVendorRepository;
    }

    /**
     * Daily SLA evaluation (configurable via app.sla.scheduler.cron, default: 2 AM every day).
     * For each active policy:
     * 1. Snapshot vendor metrics for the evaluation window.
     * 2. Compare against thresholds and write VendorSLABreach records.
     * 3. Take the configured action (WARN, RESTRICT_LISTINGS, SUSPEND).
     */
    @Scheduled(cron = "${app.sla.scheduler.cron:0 0 2 * * *}")
    public void runEvaluationCycle() {
        log.info("[SLA SCHEDULER] Starting daily SLA evaluation");
        List<VendorSLAPolicy> activePolicies = policyRepository.findAllByActiveTrue();
        log.info("[SLA SCHEDULER] Evaluating {} active SLA policies", activePolicies.size());

        for (VendorSLAPolicy policy : activePolicies) {
            try {
                evaluatePolicy(policy);
            } catch (Exception e) {
                log.error("[SLA SCHEDULER] Error evaluating policy {} for marketplace {}: {}",
                        policy.getId(), policy.getMarketplaceId(), e.getMessage());
            }
        }
        log.info("[SLA SCHEDULER] SLA evaluation cycle complete");
    }

    // -------------------------------------------------------------------------
    // Per-policy evaluation
    // -------------------------------------------------------------------------

    private void evaluatePolicy(VendorSLAPolicy policy) {
        long marketplaceId = policy.getMarketplaceId();
        var vendorPage = marketplaceVendorRepository.findByMarketplaceIdAndStatus(
                marketplaceId, VendorStatus.APPROVED, PageRequest.of(0, Integer.MAX_VALUE));

        log.info("[SLA SCHEDULER] Policy {} — evaluating {} vendors in marketplace {}",
                policy.getId(), vendorPage.getTotalElements(), marketplaceId);

        for (MarketplaceVendor vendor : vendorPage.getContent()) {
            try {
                evaluateVendor(vendor, policy);
            } catch (Exception e) {
                log.error("[SLA SCHEDULER] Error evaluating vendor {} for policy {}: {}",
                        vendor.getId(), policy.getId(), e.getMessage());
            }
        }
    }

    private void evaluateVendor(MarketplaceVendor vendor, VendorSLAPolicy policy) {
        long vendorId = vendor.getId();
        long marketplaceId = policy.getMarketplaceId();
        int windowDays = policy.getEvaluationWindowDays();
        LocalDate today = LocalDate.now();

        Instant windowStart = Instant.now().minus(windowDays, ChronoUnit.DAYS);
        Instant windowEnd = Instant.now();

        // Compute metrics
        Long totalOrders = analyticsRepository.vendorTotalOrders(vendorId, marketplaceId, windowStart, windowEnd);
        long total = totalOrders != null ? totalOrders : 0L;

        if (total == 0) return; // No orders in window — skip evaluation

        Long cancelled = analyticsRepository.vendorCancelledCount(vendorId, marketplaceId, windowStart, windowEnd);
        Long returned = analyticsRepository.vendorReturnedCount(vendorId, marketplaceId, windowStart, windowEnd);
        long cancelledCount = cancelled != null ? cancelled : 0L;
        long returnedCount = returned != null ? returned : 0L;

        VendorShipHoursProjection shipStats = analyticsRepository.vendorShipHours(
                vendorId, marketplaceId, windowStart, windowEnd, policy.getTargetShipHours());
        long shipped = shipStats != null && shipStats.getTotalShipped() != null ? shipStats.getTotalShipped() : 0L;
        long late = shipStats != null && shipStats.getTotalLate() != null ? shipStats.getTotalLate() : 0L;
        Double avgShipHours = shipStats != null ? shipStats.getAvgShipHours() : null;

        double cancellationRate = total > 0 ? (double) cancelledCount / total : 0.0;
        double refundRate = total > 0 ? (double) returnedCount / total : 0.0;
        double lateShipmentRate = shipped > 0 ? (double) late / shipped : 0.0;
        double defectRate = total > 0 ? (double) (cancelledCount + returnedCount) / total : 0.0;

        // Upsert today's metric snapshot
        VendorSLAMetric metric = metricRepository.findByVendorIdAndDate(vendorId, today)
                .orElse(new VendorSLAMetric());
        metric.setVendorId(vendorId);
        metric.setMarketplaceId(marketplaceId);
        metric.setDate(today);
        metric.setTotalOrders(total);
        metric.setShipHoursP50(avgShipHours);
        metric.setShipHoursP90(null); // Reserved for future percentile computation
        metric.setCancellationRate(cancellationRate);
        metric.setRefundRate(refundRate);
        metric.setLateShipmentRate(lateShipmentRate);
        metric.setDefectRate(defectRate);
        metricRepository.save(metric);

        // Evaluate thresholds and record breaches
        boolean anyBreach = false;
        anyBreach |= checkThreshold(vendor, policy, "cancellationRate", cancellationRate, policy.getMaxCancellationRate());
        anyBreach |= checkThreshold(vendor, policy, "refundRate", refundRate, policy.getMaxRefundRate());
        anyBreach |= checkThreshold(vendor, policy, "lateShipmentRate", lateShipmentRate, policy.getMaxLateShipmentRate());

        if (anyBreach) {
            applyBreachAction(vendor, policy);
        }
    }

    private boolean checkThreshold(MarketplaceVendor vendor, VendorSLAPolicy policy,
                                    String metricName, double actual, double threshold) {
        if (actual <= threshold) return false;

        VendorSLABreach breach = new VendorSLABreach();
        breach.setVendorId(vendor.getId());
        breach.setPolicyId(policy.getId());
        breach.setMetric(metricName);
        breach.setActualValue(actual);
        breach.setThreshold(threshold);
        breach.setDetectedAt(Instant.now());
        breach.setActionTaken(policy.getBreachAction());
        breachRepository.save(breach);

        log.warn("[SLA SCHEDULER] Vendor {} breached {} (actual={}, threshold={}), action={}",
                vendor.getId(), metricName,
                String.format("%.4f", actual), String.format("%.4f", threshold),
                policy.getBreachAction());
        return true;
    }

    private void applyBreachAction(MarketplaceVendor vendor, VendorSLAPolicy policy) {
        switch (policy.getBreachAction()) {
            case WARN -> log.warn("[SLA SCHEDULER] WARN action for vendor {} in marketplace {}",
                    vendor.getId(), policy.getMarketplaceId());
            case RESTRICT_LISTINGS -> {
                // Signal that listings should be restricted — operator reviews required
                log.warn("[SLA SCHEDULER] RESTRICT_LISTINGS action for vendor {} — manual operator review required",
                        vendor.getId());
            }
            case SUSPEND -> {
                vendor.setStatus(VendorStatus.SUSPENDED);
                vendor.setSuspendedAt(Instant.now());
                marketplaceVendorRepository.save(vendor);
                log.warn("[SLA SCHEDULER] Vendor {} SUSPENDED due to SLA breach (policy {})",
                        vendor.getId(), policy.getId());
            }
        }
    }
}
