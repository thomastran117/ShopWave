package backend.dtos.responses.order;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class OrderItemResponse {
    private Long id;
    private Long productId;
    private String productName;
    private Long variantId;
    private String variantTitle;
    private String variantSku;
    private int quantity;
    private BigDecimal unitPrice;
    private Long fulfillmentLocationId;
    private String fulfillmentLocationName;
    private boolean backorder;
    private Long bundleId;
    private String bundleName;
    private BigDecimal discountAmount;
}
