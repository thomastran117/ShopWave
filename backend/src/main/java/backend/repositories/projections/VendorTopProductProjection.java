package backend.repositories.projections;

import java.math.BigDecimal;

/** Top-product row for vendor analytics, aggregated by product. */
public interface VendorTopProductProjection {
    Long getProductId();
    String getProductName();
    Long getTotalUnitsSold();
    BigDecimal getTotalRevenue();
}
