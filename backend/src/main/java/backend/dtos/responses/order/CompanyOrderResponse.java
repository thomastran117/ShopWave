package backend.dtos.responses.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record CompanyOrderResponse(
        Long orderId,
        Long buyerUserId,
        String orderStatus,
        String currency,
        BigDecimal companyItemsTotal,
        List<OrderItemResponse> items,
        String trackingNumber,
        String carrier,
        Instant shippedAt,
        Instant deliveredAt,
        Instant returnedAt,
        String fulfillmentNote,
        Instant createdAt
) {}
