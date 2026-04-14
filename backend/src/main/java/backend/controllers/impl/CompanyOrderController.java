package backend.controllers.impl;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import backend.annotations.requireAuth.RequireAuth;
import backend.dtos.requests.order.ReturnOrderRequest;
import backend.dtos.requests.order.ShipOrderRequest;
import backend.dtos.responses.general.PagedResponse;
import backend.dtos.responses.order.CompanyOrderResponse;
import backend.exceptions.http.AppHttpException;
import backend.exceptions.http.InternalServerErrorException;
import backend.models.enums.OrderStatus;
import backend.services.intf.OrderService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("/companies/{companyId}/orders")
@RequireAuth
public class CompanyOrderController {

    private final OrderService orderService;

    public CompanyOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<PagedResponse<CompanyOrderResponse>> getCompanyOrders(
            @PathVariable long companyId,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size) {
        try {
            long userId = resolveUserId();
            return ResponseEntity.ok(orderService.getCompanyOrders(companyId, userId, status, page, size));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<CompanyOrderResponse> getCompanyOrder(
            @PathVariable long companyId,
            @PathVariable long orderId) {
        try {
            long userId = resolveUserId();
            return ResponseEntity.ok(orderService.getCompanyOrder(companyId, orderId, userId));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @PostMapping("/{orderId}/pack")
    public ResponseEntity<CompanyOrderResponse> markAsPacked(
            @PathVariable long companyId,
            @PathVariable long orderId) {
        try {
            long userId = resolveUserId();
            return ResponseEntity.ok(orderService.markAsPacked(companyId, orderId, userId));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @PostMapping("/{orderId}/ship")
    public ResponseEntity<CompanyOrderResponse> markAsShipped(
            @PathVariable long companyId,
            @PathVariable long orderId,
            @RequestBody @Valid ShipOrderRequest request) {
        try {
            long userId = resolveUserId();
            return ResponseEntity.ok(orderService.markAsShipped(companyId, orderId, userId, request));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @PostMapping("/{orderId}/deliver")
    public ResponseEntity<CompanyOrderResponse> markAsDelivered(
            @PathVariable long companyId,
            @PathVariable long orderId) {
        try {
            long userId = resolveUserId();
            return ResponseEntity.ok(orderService.markAsDelivered(companyId, orderId, userId));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @PostMapping("/{orderId}/return")
    public ResponseEntity<CompanyOrderResponse> initiateReturn(
            @PathVariable long companyId,
            @PathVariable long orderId,
            @RequestBody @Valid ReturnOrderRequest request) {
        try {
            long userId = resolveUserId();
            return ResponseEntity.ok(orderService.initiateReturn(companyId, orderId, userId, request));
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
