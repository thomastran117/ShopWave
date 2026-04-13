package backend.services.intf;

import backend.dtos.requests.discount.CreateDiscountRequest;
import backend.dtos.requests.discount.UpdateDiscountRequest;
import backend.dtos.responses.discount.DiscountResponse;
import backend.dtos.responses.general.PagedResponse;

public interface DiscountService {
    PagedResponse<DiscountResponse> listDiscounts(long companyId, long ownerId, int page, int size);
    DiscountResponse getDiscount(long companyId, long discountId, long ownerId);
    DiscountResponse createDiscount(long companyId, long ownerId, CreateDiscountRequest request);
    DiscountResponse updateDiscount(long companyId, long discountId, long ownerId, UpdateDiscountRequest request);
    void deleteDiscount(long companyId, long discountId, long ownerId);
}
