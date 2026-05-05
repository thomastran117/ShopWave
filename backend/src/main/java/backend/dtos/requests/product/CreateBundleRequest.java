package backend.dtos.requests.product;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class CreateBundleRequest {

    @NotBlank
    @Size(max = 255)
    private String name;

    private String description;

    private String thumbnailUrl;

    /** If null, auto-computed as sum of (item.quantity × product/variant price). */
    @DecimalMin("0.00")
    private BigDecimal price;

    private BigDecimal compareAtPrice;

    @Size(min = 3, max = 3)
    private String currency;

    private boolean listed = true;

    @NotNull
    @Size(min = 1, max = 10)
    @Valid
    private List<BundleItemRequest> items;
}
