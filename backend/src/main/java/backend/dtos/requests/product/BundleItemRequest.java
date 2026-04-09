package backend.dtos.requests.product;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BundleItemRequest {

    @NotNull
    private Long productId;

    private Long variantId;

    @Min(1)
    private int quantity = 1;

    private int displayOrder = 0;
}
