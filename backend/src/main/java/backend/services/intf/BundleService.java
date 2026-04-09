package backend.services.intf;

import backend.dtos.requests.product.CreateBundleRequest;
import backend.dtos.requests.product.UpdateBundleRequest;
import backend.dtos.responses.general.PagedResponse;
import backend.dtos.responses.product.BundleResponse;
import backend.models.enums.ProductStatus;

public interface BundleService {
    // Owner-authenticated CRUD
    PagedResponse<BundleResponse> listBundles(long companyId, long ownerId, ProductStatus status, int page, int size);
    BundleResponse createBundle(long companyId, long ownerId, CreateBundleRequest request);
    BundleResponse getBundle(long companyId, long bundleId, long ownerId);
    BundleResponse updateBundle(long companyId, long bundleId, long ownerId, UpdateBundleRequest request);
    void deleteBundle(long companyId, long bundleId, long ownerId);

    // Public read (no ownership check — for product-route discoverability)
    PagedResponse<BundleResponse> listBundles(long companyId, ProductStatus status, int page, int size);
    BundleResponse getBundle(long companyId, long bundleId);
}
