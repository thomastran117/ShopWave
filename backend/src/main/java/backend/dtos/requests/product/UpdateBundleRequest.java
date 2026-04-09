package backend.dtos.requests.product;

import backend.models.enums.ProductStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class UpdateBundleRequest {

    @Size(max = 255)
    private String name;

    private String description;

    private String thumbnailUrl;

    /** If null and items are being replaced, price is auto-recomputed from new items. */
    @DecimalMin("0.00")
    private BigDecimal price;

    private BigDecimal compareAtPrice;

    private ProductStatus status;

    private Boolean listed;

    /** If provided, replaces the entire items list. Max 10 items. */
    @Size(max = 10)
    @Valid
    private List<BundleItemRequest> items;
}
