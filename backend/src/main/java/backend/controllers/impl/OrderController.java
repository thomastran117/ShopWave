package backend.controllers.impl;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import backend.annotations.requireAuth.RequireAuth;
import backend.dtos.requests.issue.ResolveWithReplacementRequest;
import backend.dtos.requests.order.CreateOrderRequest;
import backend.dtos.responses.general.PagedResponse;
import backend.dtos.responses.order.OrderResponse;
import backend.dtos.responses.return_.ReturnResponse;
import backend.exceptions.http.AppHttpException;
import backend.exceptions.http.InternalServerErrorException;
import backend.models.enums.OrderStatus;
import backend.services.intf.OrderService;
import backend.services.intf.PaymentService;
import backend.services.intf.ReplacementOrderService;
import backend.services.intf.ReturnService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final PaymentService paymentService;
    private final ReturnService returnService;
    private final ReplacementOrderService replacementOrderService;

    public OrderController(OrderService orderService, PaymentService paymentService,
                           ReturnService returnService, ReplacementOrderService replacementOrderService) {
        this.orderService = orderService;
        this.paymentService = paymentService;
        this.returnService = returnService;
        this.replacementOrderService = replacementOrderService;
    }

    @PostMapping
    @RequireAuth
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        try {
            long userId = resolveUserId();
            return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(userId, request));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @GetMapping
    @RequireAuth
    public ResponseEntity<PagedResponse<OrderResponse>> getOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        try {
            long userId = resolveUserId();
            return ResponseEntity.ok(orderService.getOrders(userId, status, page, size, sort, direction));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @GetMapping("/{id}")
    @RequireAuth
    public ResponseEntity<OrderResponse> getOrder(@PathVariable long id) {
        try {
            long userId = resolveUserId();
            return ResponseEntity.ok(orderService.getOrder(id, userId));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @PostMapping("/{id}/cancel")
    @RequireAuth
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable long id) {
        try {
            long userId = resolveUserId();
            return ResponseEntity.ok(orderService.cancelOrder(id, userId));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @PostMapping("/webhook/stripe")
    public ResponseEntity<Void> stripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        try {
            PaymentService.WebhookEvent event = paymentService.constructWebhookEvent(payload, sigHeader);

            switch (event.type()) {
                case "payment_intent.succeeded"      -> orderService.handlePaymentSuccess(event.objectId());
                case "payment_intent.payment_failed" -> orderService.handlePaymentFailure(event.objectId());
                case "charge.refunded" -> {
                    String refundId = event.metadata().get("refundId");
                    if (refundId != null && !refundId.isBlank()) {
                        returnService.handleRefundWebhookEvent(
                                refundId,
                                event.metadata().get("refundStatus"),
                                Long.parseLong(event.metadata().getOrDefault("refundAmountCents", "0")));
                    }
                }
                case "refund.updated" -> {
                    if (event.objectId() != null) {
                        returnService.handleRefundWebhookEvent(
                                event.objectId(),
                                event.metadata().getOrDefault("refundStatus", "pending"),
                                Long.parseLong(event.metadata().getOrDefault("refundAmountCents", "0")));
                    }
                }
                default -> { }
            }

            return ResponseEntity.ok().build();
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @PostMapping("/support/orders/{orderId}/partial-refund")
    @RequireAuth
    public ResponseEntity<ReturnResponse> issuePartialRefund(
            @PathVariable long orderId,
            @RequestParam long amountCents,
            @RequestParam(required = false) String reason) {
        try {
            return ResponseEntity.ok(returnService.issuePartialRefund(orderId, amountCents, reason, resolveUserId()));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @PostMapping("/support/orders/{orderId}/replacement")
    @RequireAuth
    public ResponseEntity<OrderResponse> createReplacement(
            @PathVariable long orderId,
            @Valid @RequestBody ResolveWithReplacementRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(replacementOrderService.createReplacement(orderId, request, resolveUserId()));
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
