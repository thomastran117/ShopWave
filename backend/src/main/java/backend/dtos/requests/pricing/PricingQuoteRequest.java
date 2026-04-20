package backend.dtos.requests.pricing;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * Stateless quote request. Server-authoritative pricing: the caller sends only
 * (productId, variantId?, quantity) per line; the engine looks up the current unit price.
 */
@Getter
@Setter
@NoArgsConstructor
public class PricingQuoteRequest {

    @NotEmpty(message = "items must contain at least one line")
    @Valid
    private List<Item> items;

    private String couponCode;
    private String currency;
    private BigDecimal shippingAmount;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Item {
        @NotNull
        private Long productId;

        /** Optional — when set, overrides product-level price. */
        private Long variantId;

        @Min(value = 1, message = "quantity must be >= 1")
        private int quantity;
    }
}
