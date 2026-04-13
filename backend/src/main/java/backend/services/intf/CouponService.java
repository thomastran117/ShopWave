package backend.services.intf;

import backend.dtos.requests.coupon.CreateCouponRequest;
import backend.dtos.requests.coupon.UpdateCouponRequest;
import backend.dtos.responses.coupon.CouponResponse;
import backend.dtos.responses.general.PagedResponse;

public interface CouponService {
    PagedResponse<CouponResponse> listCoupons(long companyId, long ownerId, int page, int size);
    CouponResponse getCoupon(long companyId, long couponId, long ownerId);
    CouponResponse createCoupon(long companyId, long ownerId, CreateCouponRequest request);
    CouponResponse updateCoupon(long companyId, long couponId, long ownerId, UpdateCouponRequest request);
    void deleteCoupon(long companyId, long couponId, long ownerId);
}
