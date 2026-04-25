package backend.controllers.impl;

import backend.annotations.requireAuth.RequireAuth;
import backend.dtos.requests.order.CancelSubOrderRequest;
import backend.dtos.requests.order.ShipSubOrderRequest;
import backend.dtos.responses.general.PagedResponse;
import backend.dtos.responses.order.CommissionRecordResponse;
import backend.dtos.responses.order.SubOrderResponse;
import backend.exceptions.http.AppHttpException;
import backend.exceptions.http.InternalServerErrorException;
import backend.models.enums.SubOrderStatus;
import backend.services.intf.SubOrderService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/vendors/{vendorId}/sub-orders")
@RequireAuth
public class SubOrderController {

    private final SubOrderService subOrderService;

    public SubOrderController(SubOrderService subOrderService) {
        this.subOrderService = subOrderService;
    }

    @GetMapping
    public ResponseEntity<PagedResponse<SubOrderResponse>> list(
            @PathVariable long vendorId,
            @RequestParam(required = false) SubOrderStatus status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size) {
        try {
            return ResponseEntity.ok(subOrderService.listVendorSubOrders(vendorId, status, page, size));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @GetMapping("/{subOrderId}")
    public ResponseEntity<SubOrderResponse> get(
            @PathVariable long vendorId,
            @PathVariable long subOrderId) {
        try {
            return ResponseEntity.ok(subOrderService.getSubOrder(subOrderId, vendorId));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @PostMapping("/{subOrderId}/pack")
    public ResponseEntity<SubOrderResponse> pack(
            @PathVariable long vendorId,
            @PathVariable long subOrderId) {
        try {
            return ResponseEntity.ok(subOrderService.markPacked(subOrderId, vendorId));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @PostMapping("/{subOrderId}/ship")
    public ResponseEntity<SubOrderResponse> ship(
            @PathVariable long vendorId,
            @PathVariable long subOrderId,
            @Valid @RequestBody ShipSubOrderRequest request) {
        try {
            return ResponseEntity.ok(subOrderService.markShipped(subOrderId, vendorId, request));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @PostMapping("/{subOrderId}/deliver")
    public ResponseEntity<SubOrderResponse> deliver(
            @PathVariable long vendorId,
            @PathVariable long subOrderId) {
        try {
            return ResponseEntity.ok(subOrderService.markDelivered(subOrderId, vendorId));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @PostMapping("/{subOrderId}/cancel")
    public ResponseEntity<SubOrderResponse> cancel(
            @PathVariable long vendorId,
            @PathVariable long subOrderId,
            @Valid @RequestBody CancelSubOrderRequest request) {
        try {
            return ResponseEntity.ok(subOrderService.cancelSubOrder(subOrderId, vendorId, request));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @GetMapping("/{subOrderId}/commission")
    public ResponseEntity<CommissionRecordResponse> commission(
            @PathVariable long vendorId,
            @PathVariable long subOrderId) {
        try {
            return ResponseEntity.ok(subOrderService.getCommissionRecord(subOrderId, vendorId));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

}
