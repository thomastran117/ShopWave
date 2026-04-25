package backend.dtos.responses.vendor;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@AllArgsConstructor
public class VendorAnalyticsSummaryResponse {
    private long vendorId;
    private long marketplaceId;
    private int windowDays;
    private Instant from;
    private Instant to;
    private long totalSubOrders;
    private BigDecimal totalGrossRevenue;
    private BigDecimal totalCommission;
    private BigDecimal totalNetRevenue;
    private BigDecimal avgOrderValue;
    private double cancellationRate;
    private double refundRate;
    private double lateShipmentRate;
    private Double avgShipHours;
}
