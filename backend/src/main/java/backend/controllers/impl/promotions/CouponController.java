package backend.controllers.impl.promotions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import backend.annotations.requireAuth.RequireAuth;
import backend.dtos.requests.coupon.CreateCouponRequest;
import backend.dtos.requests.coupon.UpdateCouponRequest;
import backend.dtos.responses.coupon.CouponResponse;
import backend.dtos.responses.general.PagedResponse;
import backend.exceptions.http.AppHttpException;
import backend.exceptions.http.InternalServerErrorException;
import backend.services.intf.promotions.CouponService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("/companies/{companyId}/coupons")
public class CouponController {

    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @GetMapping
    @RequireAuth
    public ResponseEntity<PagedResponse<CouponResponse>> listCoupons(
            @PathVariable long companyId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size) {
        try {
            long userId = resolveUserId();
            return ResponseEntity.ok(couponService.listCoupons(companyId, userId, page, size));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @GetMapping("/{couponId}")
    @RequireAuth
    public ResponseEntity<CouponResponse> getCoupon(
            @PathVariable long companyId,
            @PathVariable long couponId) {
        try {
            long userId = resolveUserId();
            return ResponseEntity.ok(couponService.getCoupon(companyId, couponId, userId));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @PostMapping
    @RequireAuth
    public ResponseEntity<CouponResponse> createCoupon(
            @PathVariable long companyId,
            @Valid @RequestBody CreateCouponRequest request) {
        try {
            long userId = resolveUserId();
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(couponService.createCoupon(companyId, userId, request));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @PatchMapping("/{couponId}")
    @RequireAuth
    public ResponseEntity<CouponResponse> updateCoupon(
            @PathVariable long companyId,
            @PathVariable long couponId,
            @Valid @RequestBody UpdateCouponRequest request) {
        try {
            long userId = resolveUserId();
            return ResponseEntity.ok(couponService.updateCoupon(companyId, couponId, userId, request));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @DeleteMapping("/{couponId}")
    @RequireAuth
    public ResponseEntity<Void> deleteCoupon(
            @PathVariable long companyId,
            @PathVariable long couponId) {
        try {
            long userId = resolveUserId();
            couponService.deleteCoupon(companyId, couponId, userId);
            return ResponseEntity.noContent().build();
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    private long resolveUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ((Number) auth.getPrincipal()).longValue();
    }
}
