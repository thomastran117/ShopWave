package backend.dtos.requests.wishlist;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateWishlistRequest {

    @Size(max = 100)
    private String name;

    private Boolean isPublic;
}
