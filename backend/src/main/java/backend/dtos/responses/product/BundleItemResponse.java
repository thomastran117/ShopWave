package backend.dtos.responses.product;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BundleItemResponse {
    private Long id;
    private Long productId;
    private String productName;
    private Long variantId;
    private String variantSku;
    private String variantTitle;
    private int quantity;
    private int displayOrder;
}
