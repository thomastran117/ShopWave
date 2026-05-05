package backend.services.intf.wishlist;

import backend.dtos.requests.wishlist.AddWishlistItemRequest;
import backend.dtos.requests.wishlist.CreateWishlistRequest;
import backend.dtos.requests.wishlist.UpdateWishlistRequest;
import backend.dtos.responses.wishlist.WishlistItemResponse;
import backend.dtos.responses.wishlist.WishlistResponse;
import backend.dtos.responses.wishlist.WishlistSummaryResponse;

import java.util.List;

public interface WishlistService {

    List<WishlistSummaryResponse> listWishlists(long userId);

    WishlistResponse getWishlist(long userId, long wishlistId);

    WishlistResponse createWishlist(long userId, CreateWishlistRequest request);

    WishlistResponse updateWishlist(long userId, long wishlistId, UpdateWishlistRequest request);

    void deleteWishlist(long userId, long wishlistId);

    WishlistItemResponse addItem(long userId, long wishlistId, AddWishlistItemRequest request);

    void removeItem(long userId, long wishlistId, long itemId);
}
