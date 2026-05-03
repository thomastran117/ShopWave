package backend.controllers.impl.vendors;

import backend.annotations.requireAuth.RequireAuth;
import backend.dtos.responses.vendor.VendorAnalyticsSummaryResponse;
import backend.dtos.responses.vendor.VendorOrdersMetricResponse;
import backend.dtos.responses.vendor.VendorPayoutsMetricResponse;
import backend.dtos.responses.vendor.VendorRefundsMetricResponse;
import backend.dtos.responses.vendor.VendorRevenueResponse;
import backend.dtos.responses.vendor.VendorTopProductsResponse;
import backend.exceptions.http.AppHttpException;
import backend.exceptions.http.InternalServerErrorException;
import backend.services.intf.vendors.VendorAnalyticsService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequireAuth
public class VendorAnalyticsController {

    private final VendorAnalyticsService vendorAnalyticsService;

    public VendorAnalyticsController(VendorAnalyticsService vendorAnalyticsService) {
        this.vendorAnalyticsService = vendorAnalyticsService;
    }

    @GetMapping("/marketplaces/{marketplaceId}/vendors/{vendorId}/analytics/summary")
    public ResponseEntity<VendorAnalyticsSummaryResponse> getSummary(
            @PathVariable long marketplaceId,
            @PathVariable long vendorId,
            @RequestParam(defaultValue = "30") int days) {
        try {
            return ResponseEntity.ok(vendorAnalyticsService.getSummary(vendorId, marketplaceId, days, resolveUserId()));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @GetMapping("/marketplaces/{marketplaceId}/vendors/{vendorId}/analytics/revenue")
    public ResponseEntity<VendorRevenueResponse> getRevenue(
            @PathVariable long marketplaceId,
            @PathVariable long vendorId,
            @RequestParam(defaultValue = "30") int days) {
        try {
            return ResponseEntity.ok(vendorAnalyticsService.getRevenue(vendorId, marketplaceId, days, resolveUserId()));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @GetMapping("/marketplaces/{marketplaceId}/vendors/{vendorId}/analytics/top-products")
    public ResponseEntity<VendorTopProductsResponse> getTopProducts(
            @PathVariable long marketplaceId,
            @PathVariable long vendorId,
            @RequestParam(defaultValue = "30") int days,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            return ResponseEntity.ok(vendorAnalyticsService.getTopProducts(vendorId, marketplaceId, days, limit, resolveUserId()));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @GetMapping("/marketplaces/{marketplaceId}/vendors/{vendorId}/analytics/orders")
    public ResponseEntity<VendorOrdersMetricResponse> getOrders(
            @PathVariable long marketplaceId,
            @PathVariable long vendorId,
            @RequestParam(defaultValue = "30") int days) {
        try {
            return ResponseEntity.ok(vendorAnalyticsService.getOrders(vendorId, marketplaceId, days, resolveUserId()));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @GetMapping("/marketplaces/{marketplaceId}/vendors/{vendorId}/analytics/refunds")
    public ResponseEntity<VendorRefundsMetricResponse> getRefunds(
            @PathVariable long marketplaceId,
            @PathVariable long vendorId,
            @RequestParam(defaultValue = "30") int days) {
        try {
            return ResponseEntity.ok(vendorAnalyticsService.getRefunds(vendorId, marketplaceId, days, resolveUserId()));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @GetMapping("/marketplaces/{marketplaceId}/vendors/{vendorId}/analytics/payouts")
    public ResponseEntity<VendorPayoutsMetricResponse> getPayouts(
            @PathVariable long marketplaceId,
            @PathVariable long vendorId,
            @RequestParam(defaultValue = "10") int recent) {
        try {
            return ResponseEntity.ok(vendorAnalyticsService.getPayouts(vendorId, marketplaceId, recent, resolveUserId()));
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
