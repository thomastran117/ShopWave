package backend.services.intf.inventory;

import backend.dtos.requests.inventory.CreateRestockRequest;
import backend.dtos.requests.inventory.UpdateRestockRequest;
import backend.dtos.responses.general.PagedResponse;
import backend.dtos.responses.inventory.RestockRequestResponse;
import backend.models.enums.RestockStatus;

public interface RestockService {
    PagedResponse<RestockRequestResponse> listRestockRequests(long companyId, long ownerId, RestockStatus status, Long productId, int page, int size);
    RestockRequestResponse createRestockRequest(long companyId, long ownerId, CreateRestockRequest request);
    RestockRequestResponse getRestockRequest(long companyId, long restockId, long ownerId);
    RestockRequestResponse updateRestockRequest(long companyId, long restockId, long ownerId, UpdateRestockRequest request);
    void deleteRestockRequest(long companyId, long restockId, long ownerId);
}
