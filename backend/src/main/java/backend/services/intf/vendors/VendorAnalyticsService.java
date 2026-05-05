package backend.services.intf.vendors;

import backend.dtos.responses.marketplace.MarketplaceAnalyticsSummaryResponse;
import backend.dtos.responses.marketplace.TopVendorResponse;
import backend.dtos.responses.vendor.VendorAnalyticsSummaryResponse;
import backend.dtos.responses.vendor.VendorOrdersMetricResponse;
import backend.dtos.responses.vendor.VendorPayoutsMetricResponse;
import backend.dtos.responses.vendor.VendorRefundsMetricResponse;
import backend.dtos.responses.vendor.VendorRevenueResponse;
import backend.dtos.responses.vendor.VendorTopProductsResponse;

import java.util.List;

public interface VendorAnalyticsService {

    // Vendor-scoped endpoints
    VendorAnalyticsSummaryResponse getSummary(long vendorId, long marketplaceId, int lookbackDays, long actorUserId);
    VendorRevenueResponse getRevenue(long vendorId, long marketplaceId, int lookbackDays, long actorUserId);
    VendorTopProductsResponse getTopProducts(long vendorId, long marketplaceId, int lookbackDays, int limit, long actorUserId);
    VendorOrdersMetricResponse getOrders(long vendorId, long marketplaceId, int lookbackDays, long actorUserId);
    VendorRefundsMetricResponse getRefunds(long vendorId, long marketplaceId, int lookbackDays, long actorUserId);
    VendorPayoutsMetricResponse getPayouts(long vendorId, long marketplaceId, int recentCount, long actorUserId);

    // Marketplace operator endpoints
    MarketplaceAnalyticsSummaryResponse getMarketplaceSummary(long marketplaceId, long operatorUserId, int lookbackDays);
    List<TopVendorResponse> getTopVendors(long marketplaceId, long operatorUserId, int lookbackDays, int limit);
}
