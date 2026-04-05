package backend.controllers.impl;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import backend.annotations.requireAuth.RequireAuth;
import backend.dtos.requests.inventory.AdjustStockRequest;
import backend.dtos.requests.inventory.BulkAdjustRequest;
import backend.dtos.requests.inventory.UpdateInventorySettingsRequest;
import backend.dtos.responses.general.PagedResponse;
import backend.dtos.responses.inventory.AdjustmentResponse;
import backend.dtos.responses.inventory.InventoryItemResponse;
import backend.dtos.responses.inventory.InventorySummaryResponse;
import backend.dtos.responses.inventory.ProductSalesMetricResponse;
import backend.exceptions.http.AppHttpException;
import backend.exceptions.http.InternalServerErrorException;
import backend.services.intf.InventoryService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.List;

@RestController
@RequestMapping("/companies/{companyId}/inventory")
@RequireAuth
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public ResponseEntity<PagedResponse<InventoryItemResponse>> getInventory(
            @PathVariable long companyId,
            @RequestParam(required = false) String stockStatus,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size,
            @RequestParam(defaultValue = "updatedAt") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        try {
            long userId = resolveUserId();
            return ResponseEntity.ok(inventoryService.getInventory(companyId, userId, stockStatus, q, page, size, sort, direction));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @GetMapping("/summary")
    public ResponseEntity<InventorySummaryResponse> getSummary(
            @PathVariable long companyId) {
        try {
            long userId = resolveUserId();
            return ResponseEntity.ok(inventoryService.getSummary(companyId, userId));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @GetMapping("/{productId}")
    public ResponseEntity<InventoryItemResponse> getInventoryItem(
            @PathVariable long companyId,
            @PathVariable long productId) {
        try {
            long userId = resolveUserId();
            return ResponseEntity.ok(inventoryService.getInventoryItem(companyId, productId, userId));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @GetMapping("/{productId}/history")
    public ResponseEntity<PagedResponse<AdjustmentResponse>> getAdjustmentHistory(
            @PathVariable long companyId,
            @PathVariable long productId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size) {
        try {
            long userId = resolveUserId();
            return ResponseEntity.ok(inventoryService.getAdjustmentHistory(companyId, productId, userId, page, size));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @PostMapping("/{productId}/adjust")
    public ResponseEntity<InventoryItemResponse> adjustStock(
            @PathVariable long companyId,
            @PathVariable long productId,
            @Valid @RequestBody AdjustStockRequest request) {
        try {
            long userId = resolveUserId();
            return ResponseEntity.ok(inventoryService.adjustStock(companyId, productId, userId, request));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @PostMapping("/bulk-adjust")
    public ResponseEntity<List<InventoryItemResponse>> bulkAdjust(
            @PathVariable long companyId,
            @Valid @RequestBody BulkAdjustRequest request) {
        try {
            long userId = resolveUserId();
            return ResponseEntity.ok(inventoryService.bulkAdjust(companyId, userId, request));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @GetMapping("/analytics/top-purchased")
    public ResponseEntity<List<ProductSalesMetricResponse>> getTopPurchasedProducts(
            @PathVariable long companyId,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit) {
        try {
            long userId = resolveUserId();
            return ResponseEntity.ok(inventoryService.getTopPurchasedProducts(companyId, userId, limit));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @GetMapping("/analytics/top-revenue")
    public ResponseEntity<List<ProductSalesMetricResponse>> getTopRevenueProducts(
            @PathVariable long companyId,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit) {
        try {
            long userId = resolveUserId();
            return ResponseEntity.ok(inventoryService.getTopRevenueProducts(companyId, userId, limit));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @GetMapping("/analytics/never-sold")
    public ResponseEntity<List<ProductSalesMetricResponse>> getNeverSoldProducts(
            @PathVariable long companyId,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int limit) {
        try {
            long userId = resolveUserId();
            return ResponseEntity.ok(inventoryService.getNeverSoldProducts(companyId, userId, limit));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @PatchMapping("/{productId}/settings")
    public ResponseEntity<InventoryItemResponse> updateSettings(
            @PathVariable long companyId,
            @PathVariable long productId,
            @Valid @RequestBody UpdateInventorySettingsRequest request) {
        try {
            long userId = resolveUserId();
            return ResponseEntity.ok(inventoryService.updateSettings(companyId, productId, userId, request));
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
