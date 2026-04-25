package backend.controllers.impl;

import backend.annotations.requireAuth.RequireAuth;
import backend.dtos.requests.marketplace.VendorAdjustmentRequest;
import backend.dtos.responses.general.PagedResponse;
import backend.dtos.responses.vendor.VendorAdjustmentResponse;
import backend.dtos.responses.vendor.VendorBalanceResponse;
import backend.dtos.responses.vendor.VendorPayoutResponse;
import backend.exceptions.http.AppHttpException;
import backend.exceptions.http.InternalServerErrorException;
import backend.models.enums.PayoutStatus;
import backend.services.intf.VendorPayoutService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequireAuth
public class VendorPayoutController {

    private final VendorPayoutService vendorPayoutService;

    public VendorPayoutController(VendorPayoutService vendorPayoutService) {
        this.vendorPayoutService = vendorPayoutService;
    }

    // -------------------------------------------------------------------------
    // Vendor self-service
    // -------------------------------------------------------------------------

    @GetMapping("/vendors/{vendorId}/balance")
    public ResponseEntity<VendorBalanceResponse> getBalance(@PathVariable long vendorId) {
        try {
            return ResponseEntity.ok(vendorPayoutService.getBalance(vendorId));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @GetMapping("/vendors/{vendorId}/payouts")
    public ResponseEntity<PagedResponse<VendorPayoutResponse>> listPayouts(
            @PathVariable long vendorId,
            @RequestParam(required = false) PayoutStatus status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size) {
        try {
            return ResponseEntity.ok(vendorPayoutService.listPayouts(vendorId, status, page, size));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @GetMapping("/vendors/{vendorId}/payouts/{payoutId}")
    public ResponseEntity<VendorPayoutResponse> getPayoutDetail(
            @PathVariable long vendorId,
            @PathVariable long payoutId) {
        try {
            return ResponseEntity.ok(vendorPayoutService.getPayoutDetail(payoutId, vendorId));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    // -------------------------------------------------------------------------
    // Operator actions
    // -------------------------------------------------------------------------

    @PostMapping("/marketplaces/{marketplaceId}/payouts/run")
    public ResponseEntity<VendorPayoutResponse> triggerManualPayout(
            @PathVariable long marketplaceId,
            @RequestParam long vendorId) {
        try {
            long userId = resolveUserId();
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(vendorPayoutService.triggerManualPayout(vendorId, marketplaceId, userId));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @PostMapping("/marketplaces/{marketplaceId}/vendors/{vendorId}/adjustments")
    public ResponseEntity<VendorAdjustmentResponse> createAdjustment(
            @PathVariable long marketplaceId,
            @PathVariable long vendorId,
            @Valid @RequestBody VendorAdjustmentRequest request) {
        try {
            long userId = resolveUserId();
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(vendorPayoutService.createAdjustment(vendorId, userId, request));
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
