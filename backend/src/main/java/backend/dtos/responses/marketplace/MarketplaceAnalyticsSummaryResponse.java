package backend.dtos.responses.marketplace;

import backend.dtos.responses.operations.DailyPoint;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@AllArgsConstructor
public class MarketplaceAnalyticsSummaryResponse {
    private long marketplaceId;
    private int windowDays;
    private Instant from;
    private Instant to;
    private long totalOrders;
    private BigDecimal gmv;
    private BigDecimal totalCommission;
    private double takeRate;
    private long activeVendors;
    private List<DailyPoint> ordersDaily;
}
