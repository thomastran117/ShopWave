package backend.services.intf.inventory;

import backend.dtos.requests.inventory.AdjustStockRequest;
import backend.dtos.requests.inventory.CreateLocationRequest;
import backend.dtos.requests.inventory.SetLocationStockRequest;
import backend.dtos.requests.inventory.UpdateLocationRequest;
import backend.dtos.responses.inventory.LocationResponse;
import backend.dtos.responses.inventory.LocationStockResponse;

import java.util.List;

public interface LocationInventoryService {

    List<LocationResponse> getLocations(long companyId, long ownerId);

    LocationResponse getLocation(long companyId, long locationId, long ownerId);

    LocationResponse createLocation(long companyId, long ownerId, CreateLocationRequest request);

    LocationResponse updateLocation(long companyId, long locationId, long ownerId, UpdateLocationRequest request);

    void deleteLocation(long companyId, long locationId, long ownerId);

    List<LocationStockResponse> getLocationStock(long companyId, long locationId, long ownerId);

    List<LocationStockResponse> getProductLocationStocks(long companyId, long productId, long ownerId);

    /** variantId=null means product-level stock. */
    LocationStockResponse setLocationStock(long companyId, long locationId, long productId,
                                           long ownerId, SetLocationStockRequest request, Long variantId);

    /** variantId=null means product-level stock. Reuses AdjustStockRequest (delta, reason, note). */
    LocationStockResponse adjustLocationStock(long companyId, long locationId, long productId,
                                              long ownerId, AdjustStockRequest request, Long variantId);
}
