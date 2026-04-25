package backend.dtos.responses.order;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@AllArgsConstructor
public class SubOrderResponse {
    private Long id;
    private Long orderId;
    private Long marketplaceVendorId;
    private Long marketplaceId;
    private String vendorCompanyName;
    private String status;
    private BigDecimal subtotal;
    private BigDecimal totalAmount;
    private String currency;
    private BigDecimal commissionAmount;
    private BigDecimal netVendorAmount;
    private String trackingNumber;
    private String carrier;
    private String fulfillmentNote;
    private String cancellationReason;
    private Instant paidAt;
    private Instant packedAt;
    private Instant shippedAt;
    private Instant deliveredAt;
    private Instant cancelledAt;
    private Instant createdAt;
    private Instant updatedAt;
    private List<OrderItemResponse> items;
}
