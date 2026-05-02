package backend.services.impl.vendors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import backend.dtos.requests.sla.CreateSLAPolicyRequest;
import backend.dtos.responses.general.PagedResponse;
import backend.dtos.responses.sla.VendorSLABreachResponse;
import backend.dtos.responses.sla.VendorSLAMetricResponse;
import backend.dtos.responses.sla.VendorSLAPolicyResponse;
import backend.exceptions.http.BadRequestException;
import backend.exceptions.http.ForbiddenException;
import backend.exceptions.http.ResourceNotFoundException;
import backend.models.core.VendorSLABreach;
import backend.models.core.VendorSLAMetric;
import backend.models.core.VendorSLAPolicy;
import backend.models.enums.SLABreachAction;
import backend.repositories.MarketplaceProfileRepository;
import backend.repositories.VendorSLABreachRepository;
import backend.repositories.VendorSLAMetricRepository;
import backend.repositories.VendorSLAPolicyRepository;
import backend.services.intf.vendors.VendorSLAService;

import java.time.Instant;
import java.util.List;

@Service
public class VendorSLAServiceImpl implements VendorSLAService {

    private final VendorSLAPolicyRepository policyRepository;
    private final VendorSLAMetricRepository metricRepository;
    private final VendorSLABreachRepository breachRepository;
    private final MarketplaceProfileRepository marketplaceProfileRepository;

    public VendorSLAServiceImpl(
            VendorSLAPolicyRepository policyRepository,
            VendorSLAMetricRepository metricRepository,
            VendorSLABreachRepository breachRepository,
            MarketplaceProfileRepository marketplaceProfileRepository) {
        this.policyRepository = policyRepository;
        this.metricRepository = metricRepository;
        this.breachRepository = breachRepository;
        this.marketplaceProfileRepository = marketplaceProfileRepository;
    }

    // -------------------------------------------------------------------------
    // Policy management
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public VendorSLAPolicyResponse createPolicy(long marketplaceId, long operatorUserId, CreateSLAPolicyRequest request) {
        assertOperator(marketplaceId, operatorUserId);

        SLABreachAction action;
        try {
            action = SLABreachAction.valueOf(request.getBreachAction());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid breachAction: " + request.getBreachAction());
        }

        VendorSLAPolicy policy = new VendorSLAPolicy();
        policy.setMarketplaceId(marketplaceId);
        policy.setName(request.getName());
        policy.setTargetShipHours(request.getTargetShipHours());
        policy.setTargetResponseHours(request.getTargetResponseHours());
        policy.setMaxCancellationRate(request.getMaxCancellationRate());
        policy.setMaxRefundRate(request.getMaxRefundRate());
        policy.setMaxLateShipmentRate(request.getMaxLateShipmentRate());
        policy.setBreachAction(action);
        policy.setEvaluationWindowDays(request.getEvaluationWindowDays());
        policy.setActive(true);

        return toPolicyResponse(policyRepository.save(policy));
    }

    @Override
    @Transactional(readOnly = true)
    public VendorSLAPolicyResponse getActivePolicy(long marketplaceId) {
        VendorSLAPolicy policy = policyRepository.findFirstByMarketplaceIdAndActiveTrue(marketplaceId)
                .orElseThrow(() -> new ResourceNotFoundException("No active SLA policy for this marketplace"));
        return toPolicyResponse(policy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VendorSLAPolicyResponse> listPolicies(long marketplaceId) {
        return policyRepository.findByMarketplaceId(marketplaceId).stream()
                .map(this::toPolicyResponse)
                .toList();
    }

    // -------------------------------------------------------------------------
    // Metrics
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<VendorSLAMetricResponse> listMetrics(long vendorId, int page, int size) {
        int cap = Math.min(size, 90);
        var pageable = PageRequest.of(page, cap, Sort.by(Sort.Direction.DESC, "date"));
        var results = metricRepository.findByVendorId(vendorId, pageable);
        return new PagedResponse<>(results.map(this::toMetricResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public VendorSLAMetricResponse getLatestMetric(long vendorId) {
        return metricRepository.findByVendorIdOrderByDateDesc(vendorId).stream()
                .findFirst()
                .map(this::toMetricResponse)
                .orElseThrow(() -> new ResourceNotFoundException("No SLA metrics available for this vendor"));
    }

    // -------------------------------------------------------------------------
    // Breaches
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<VendorSLABreachResponse> listBreaches(long vendorId, int page, int size) {
        int cap = Math.min(size, 50);
        var pageable = PageRequest.of(page, cap, Sort.by(Sort.Direction.DESC, "detectedAt"));
        var results = breachRepository.findByVendorId(vendorId, pageable);
        return new PagedResponse<>(results.map(this::toBreachResponse));
    }

    @Override
    @Transactional
    public VendorSLABreachResponse resolveBreach(long breachId, long operatorUserId, long marketplaceId) {
        assertOperator(marketplaceId, operatorUserId);
        VendorSLABreach breach = breachRepository.findById(breachId)
                .orElseThrow(() -> new ResourceNotFoundException("Breach not found"));
        if (breach.getResolvedAt() != null) {
            throw new BadRequestException("Breach is already resolved");
        }
        breach.setResolvedAt(Instant.now());
        return toBreachResponse(breachRepository.save(breach));
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void assertOperator(long marketplaceId, long userId) {
        marketplaceProfileRepository.findByCompanyId(marketplaceId)
                .filter(p -> p.getCompany().getOwner().getId() == userId)
                .orElseThrow(() -> new ForbiddenException("You are not an operator of this marketplace"));
    }

    private VendorSLAPolicyResponse toPolicyResponse(VendorSLAPolicy p) {
        return new VendorSLAPolicyResponse(
                p.getId(), p.getMarketplaceId(), p.getName(),
                p.getTargetShipHours(), p.getTargetResponseHours(),
                p.getMaxCancellationRate(), p.getMaxRefundRate(), p.getMaxLateShipmentRate(),
                p.getBreachAction().name(), p.getEvaluationWindowDays(), p.isActive(),
                p.getCreatedAt(), p.getUpdatedAt());
    }

    private VendorSLAMetricResponse toMetricResponse(VendorSLAMetric m) {
        return new VendorSLAMetricResponse(
                m.getId(), m.getVendorId(), m.getMarketplaceId(), m.getDate(),
                m.getTotalOrders(), m.getShipHoursP50(), m.getShipHoursP90(),
                m.getCancellationRate(), m.getRefundRate(), m.getLateShipmentRate(),
                m.getDefectRate(), m.getCreatedAt());
    }

    private VendorSLABreachResponse toBreachResponse(VendorSLABreach b) {
        return new VendorSLABreachResponse(
                b.getId(), b.getVendorId(), b.getPolicyId(),
                b.getMetric(), b.getActualValue(), b.getThreshold(),
                b.getDetectedAt(), b.getResolvedAt(),
                b.getActionTaken().name(), b.getNotificationSentAt(), b.getCreatedAt());
    }
}
