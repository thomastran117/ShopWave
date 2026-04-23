package backend.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import backend.dtos.requests.issue.ResolveWithReplacementRequest;
import backend.dtos.responses.order.OrderItemResponse;
import backend.dtos.responses.order.OrderResponse;
import backend.exceptions.http.BadRequestException;
import backend.exceptions.http.ResourceNotFoundException;
import backend.models.core.Order;
import backend.models.core.OrderItem;
import backend.models.core.ProductVariant;
import backend.models.enums.FulfillmentStatus;
import backend.models.enums.OrderStatus;
import backend.repositories.OrderRepository;
import backend.repositories.ProductVariantRepository;
import backend.services.intf.ReplacementOrderService;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReplacementOrderServiceImpl implements ReplacementOrderService {

    private static final Logger log = LoggerFactory.getLogger(ReplacementOrderServiceImpl.class);

    private final OrderRepository orderRepository;
    private final ProductVariantRepository variantRepository;

    public ReplacementOrderServiceImpl(OrderRepository orderRepository,
                                       ProductVariantRepository variantRepository) {
        this.orderRepository = orderRepository;
        this.variantRepository = variantRepository;
    }

    @Override
    @Transactional
    public OrderResponse createReplacement(long originalOrderId,
                                           ResolveWithReplacementRequest request,
                                           long actorUserId) {
        Order original = orderRepository.findById(originalOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + originalOrderId));

        if (request.items().isEmpty()) {
            throw new BadRequestException("Replacement order must contain at least one item");
        }

        Order replacement = new Order();
        replacement.setUser(original.getUser());
        replacement.setTotalAmount(BigDecimal.ZERO);
        replacement.setCurrency(original.getCurrency());
        replacement.setStatus(OrderStatus.PAID);
        replacement.setReplacementOfOrderId(originalOrderId);
        replacement.setFulfillmentNote("Replacement for order #" + originalOrderId + ", authorised by staff #" + actorUserId);

        List<OrderItem> items = new ArrayList<>();
        for (ResolveWithReplacementRequest.ReplacementItem ri : request.items()) {
            ProductVariant variant = variantRepository.findById(ri.variantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Variant not found: " + ri.variantId()));

            OrderItem item = new OrderItem();
            item.setOrder(replacement);
            item.setProduct(variant.getProduct());
            item.setVariant(variant);
            item.setVariantTitle(buildVariantTitle(variant));
            item.setVariantSku(variant.getSku());
            item.setQuantity(ri.quantity());
            item.setUnitPrice(BigDecimal.ZERO);
            item.setDiscountAmount(BigDecimal.ZERO);
            item.setPromotionSavings(BigDecimal.ZERO);
            item.setProductName(variant.getProduct().getName());
            item.setFulfillmentStatus(FulfillmentStatus.PENDING);
            items.add(item);
        }
        replacement.setItems(items);

        orderRepository.save(replacement);
        log.info("Replacement order {} created for original {} by staff {}", replacement.getId(), originalOrderId, actorUserId);
        return toResponse(replacement);
    }

    private String buildVariantTitle(ProductVariant v) {
        StringBuilder sb = new StringBuilder();
        if (v.getOption1() != null) sb.append(v.getOption1());
        if (v.getOption2() != null) { if (sb.length() > 0) sb.append(" / "); sb.append(v.getOption2()); }
        if (v.getOption3() != null) { if (sb.length() > 0) sb.append(" / "); sb.append(v.getOption3()); }
        return sb.length() > 0 ? sb.toString() : null;
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(i -> new OrderItemResponse(
                        i.getId(),
                        i.getProduct() != null ? i.getProduct().getId() : null,
                        i.getProductName(),
                        i.getVariant() != null ? i.getVariant().getId() : null,
                        i.getVariantTitle(),
                        i.getVariantSku(),
                        i.getQuantity(),
                        i.getUnitPrice(),
                        null,
                        null,
                        i.getFulfillmentStatus(),
                        null,
                        null,
                        i.getDiscountAmount()))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getUser().getId(),
                itemResponses,
                order.getTotalAmount(),
                order.getCurrency(),
                order.getStatus().name(),
                null,
                null,
                order.getCouponCode(),
                order.getCouponDiscountAmount(),
                order.getTrackingNumber(),
                order.getCarrier(),
                order.getShippedAt(),
                order.getDeliveredAt(),
                order.getReturnedAt(),
                order.getFulfillmentNote(),
                order.getRefundedAmountCents(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
