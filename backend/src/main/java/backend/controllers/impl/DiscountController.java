package backend.controllers.impl;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import backend.annotations.requireAuth.RequireAuth;
import backend.dtos.requests.discount.CreateDiscountRequest;
import backend.dtos.requests.discount.UpdateDiscountRequest;
import backend.dtos.responses.discount.DiscountResponse;
import backend.dtos.responses.general.PagedResponse;
import backend.exceptions.http.AppHttpException;
import backend.exceptions.http.InternalServerErrorException;
import backend.services.intf.DiscountService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("/companies/{companyId}/discounts")
public class DiscountController {

    private final DiscountService discountService;

    public DiscountController(DiscountService discountService) {
        this.discountService = discountService;
    }

    @GetMapping
    @RequireAuth
    public ResponseEntity<PagedResponse<DiscountResponse>> listDiscounts(
            @PathVariable long companyId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size) {
        try {
            long userId = resolveUserId();
            return ResponseEntity.ok(discountService.listDiscounts(companyId, userId, page, size));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @GetMapping("/{discountId}")
    @RequireAuth
    public ResponseEntity<DiscountResponse> getDiscount(
            @PathVariable long companyId,
            @PathVariable long discountId) {
        try {
            long userId = resolveUserId();
            return ResponseEntity.ok(discountService.getDiscount(companyId, discountId, userId));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @PostMapping
    @RequireAuth
    public ResponseEntity<DiscountResponse> createDiscount(
            @PathVariable long companyId,
            @Valid @RequestBody CreateDiscountRequest request) {
        try {
            long userId = resolveUserId();
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(discountService.createDiscount(companyId, userId, request));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @PatchMapping("/{discountId}")
    @RequireAuth
    public ResponseEntity<DiscountResponse> updateDiscount(
            @PathVariable long companyId,
            @PathVariable long discountId,
            @Valid @RequestBody UpdateDiscountRequest request) {
        try {
            long userId = resolveUserId();
            return ResponseEntity.ok(discountService.updateDiscount(companyId, discountId, userId, request));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @DeleteMapping("/{discountId}")
    @RequireAuth
    public ResponseEntity<Void> deleteDiscount(
            @PathVariable long companyId,
            @PathVariable long discountId) {
        try {
            long userId = resolveUserId();
            discountService.deleteDiscount(companyId, discountId, userId);
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
