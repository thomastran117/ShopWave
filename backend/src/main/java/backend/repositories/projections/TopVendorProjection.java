package backend.repositories.projections;

import java.math.BigDecimal;

/** Top-vendor row for the marketplace analytics cross-vendor leaderboard. */
public interface TopVendorProjection {
    Long getVendorId();
    String getVendorName();
    Long getTotalSubOrders();
    BigDecimal getTotalGrossRevenue();
    BigDecimal getTotalCommission();
    Double getCancellationRate();
}
