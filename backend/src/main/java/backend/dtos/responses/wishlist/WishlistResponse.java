package backend.dtos.responses.wishlist;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@AllArgsConstructor
public class WishlistResponse {
    private Long id;
    private Long userId;
    private String name;
    private boolean isPublic;
    private List<WishlistItemResponse> items;
    private Instant createdAt;
    private Instant updatedAt;
}
