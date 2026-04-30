package backend.controllers.impl;

import backend.annotations.requireAuth.RequireAuth;
import backend.dtos.requests.wishlist.AddWishlistItemRequest;
import backend.dtos.requests.wishlist.CreateWishlistRequest;
import backend.dtos.requests.wishlist.UpdateWishlistRequest;
import backend.dtos.responses.wishlist.WishlistItemResponse;
import backend.dtos.responses.wishlist.WishlistResponse;
import backend.dtos.responses.wishlist.WishlistSummaryResponse;
import backend.exceptions.http.AppHttpException;
import backend.exceptions.http.InternalServerErrorException;
import backend.services.intf.WishlistService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/wishlists")
@RequireAuth
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @GetMapping("")
    public ResponseEntity<List<WishlistSummaryResponse>> listWishlists() {
        try {
            return ResponseEntity.ok(wishlistService.listWishlists(resolveUserId()));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<WishlistResponse> getWishlist(@PathVariable long id) {
        try {
            return ResponseEntity.ok(wishlistService.getWishlist(resolveUserId(), id));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @PostMapping("")
    public ResponseEntity<WishlistResponse> createWishlist(
            @Valid @RequestBody CreateWishlistRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(wishlistService.createWishlist(resolveUserId(), request));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<WishlistResponse> updateWishlist(
            @PathVariable long id,
            @Valid @RequestBody UpdateWishlistRequest request) {
        try {
            return ResponseEntity.ok(wishlistService.updateWishlist(resolveUserId(), id, request));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWishlist(@PathVariable long id) {
        try {
            wishlistService.deleteWishlist(resolveUserId(), id);
            return ResponseEntity.noContent().build();
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<WishlistItemResponse> addItem(
            @PathVariable long id,
            @Valid @RequestBody AddWishlistItemRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(wishlistService.addItem(resolveUserId(), id, request));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @DeleteMapping("/{id}/items/{itemId}")
    public ResponseEntity<Void> removeItem(@PathVariable long id, @PathVariable long itemId) {
        try {
            wishlistService.removeItem(resolveUserId(), id, itemId);
            return ResponseEntity.noContent().build();
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    private long resolveUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ((Number) auth.getPrincipal()).longValue();
    }
}
