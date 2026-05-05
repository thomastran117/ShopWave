package backend.dtos.responses.wishlist;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class WishlistSummaryResponse {
    private Long id;
    private Long userId;
    private String name;
    private boolean isPublic;
    private int itemCount;
    private Instant createdAt;
    private Instant updatedAt;
}
