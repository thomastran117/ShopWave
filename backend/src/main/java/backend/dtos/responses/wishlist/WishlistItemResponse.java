package backend.dtos.responses.wishlist;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class WishlistItemResponse {
    private Long id;
    private Long productId;
    private String productName;
    private Long variantId;
    private String variantSku;
    private Instant addedAt;
}
