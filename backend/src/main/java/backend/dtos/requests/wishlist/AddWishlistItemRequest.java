package backend.dtos.requests.wishlist;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AddWishlistItemRequest {

    @NotNull
    private Long productId;

    private Long variantId;
}
