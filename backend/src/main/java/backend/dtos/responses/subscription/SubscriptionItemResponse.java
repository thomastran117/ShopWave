package backend.dtos.responses.subscription;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionItemResponse {
    private Long id;
    private Long productId;
    private String productName;
    private Long variantId;
    private int quantity;
    private long unitPriceCents;
}
