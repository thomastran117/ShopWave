package backend.services.intf;

import backend.dtos.requests.inventory.AdjustStockRequest;
import backend.models.enums.ProductStatus;
import backend.dtos.requests.inventory.BulkAdjustRequest;
import backend.dtos.requests.inventory.UpdateInventorySettingsRequest;
import backend.dtos.responses.general.CursorPagedResponse;
import backend.dtos.responses.general.PagedResponse;
import backend.dtos.responses.inventory.AdjustmentResponse;
import backend.dtos.responses.inventory.InventoryItemResponse;
import backend.dtos.responses.inventory.InventorySummaryResponse;
import backend.dtos.responses.inventory.ProductSalesMetricResponse;

import java.time.Instant;
import java.util.List;

public interface InventoryService {

    CursorPagedResponse<InventoryItemResponse> getInventory(
            long companyId, long ownerId,
            String stockStatus, String q,
            String category, String brand,
            ProductStatus status, Integer minStock, Integer maxStock,
            String cursor, int size);

    InventorySummaryResponse getSummary(long companyId, long ownerId);

    InventoryItemResponse getInventoryItem(long companyId, long productId, long ownerId);

    PagedResponse<AdjustmentResponse> getAdjustmentHistory(
            long companyId, long productId, long ownerId, int page, int size);

    InventoryItemResponse adjustStock(
            long companyId, long productId, long ownerId, AdjustStockRequest request);

    List<InventoryItemResponse> bulkAdjust(
            long companyId, long ownerId, BulkAdjustRequest request);

    InventoryItemResponse updateSettings(
            long companyId, long productId, long ownerId, UpdateInventorySettingsRequest request);

    List<ProductSalesMetricResponse> getTopPurchasedProducts(long companyId, long ownerId, int limit, Instant from, Instant to);

    List<ProductSalesMetricResponse> getTopRevenueProducts(long companyId, long ownerId, int limit, Instant from, Instant to);

    List<ProductSalesMetricResponse> getNeverSoldProducts(long companyId, long ownerId, int limit);

    InventoryItemResponse adjustVariantStock(
            long companyId, long productId, long variantId, long ownerId, AdjustStockRequest request);
}
