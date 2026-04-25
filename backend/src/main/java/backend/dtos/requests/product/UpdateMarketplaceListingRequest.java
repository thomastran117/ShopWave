package backend.dtos.requests.product;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateMarketplaceListingRequest {

    @NotNull(message = "Marketplace ID is required")
    @Positive(message = "Marketplace ID must be positive")
    private Long marketplaceId;

    @NotNull(message = "Listed flag is required")
    private Boolean listed;
}
