package backend.services.impl.wishlist;

import backend.dtos.requests.wishlist.AddWishlistItemRequest;
import backend.dtos.requests.wishlist.CreateWishlistRequest;
import backend.dtos.requests.wishlist.UpdateWishlistRequest;
import backend.dtos.responses.wishlist.WishlistItemResponse;
import backend.dtos.responses.wishlist.WishlistResponse;
import backend.dtos.responses.wishlist.WishlistSummaryResponse;
import backend.exceptions.http.BadRequestException;
import backend.exceptions.http.ConflictException;
import backend.exceptions.http.ResourceNotFoundException;
import backend.models.core.Product;
import backend.models.core.ProductVariant;
import backend.models.core.User;
import backend.models.core.Wishlist;
import backend.models.core.WishlistItem;
import backend.repositories.ProductRepository;
import backend.repositories.UserRepository;
import backend.repositories.WishlistItemRepository;
import backend.repositories.WishlistRepository;
import backend.events.activity.ActivityType;
import backend.events.activity.UserActivityEvent;
import backend.services.intf.ActivityEventPublisher;
import backend.services.intf.wishlist.WishlistService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final WishlistItemRepository wishlistItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ActivityEventPublisher activityEventPublisher;

    public WishlistServiceImpl(WishlistRepository wishlistRepository,
                               WishlistItemRepository wishlistItemRepository,
                               UserRepository userRepository,
                               ProductRepository productRepository,
                               ActivityEventPublisher activityEventPublisher) {
        this.wishlistRepository = wishlistRepository;
        this.wishlistItemRepository = wishlistItemRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.activityEventPublisher = activityEventPublisher;
    }

    @Override
    @Transactional(readOnly = true)
    public List<WishlistSummaryResponse> listWishlists(long userId) {
        return wishlistRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toSummary).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public WishlistResponse getWishlist(long userId, long wishlistId) {
        return toResponse(findOwned(userId, wishlistId));
    }

    @Override
    @Transactional
    public WishlistResponse createWishlist(long userId, CreateWishlistRequest req) {
        if (wishlistRepository.existsByNameAndUserId(req.getName(), userId)) {
            throw new ConflictException("A wishlist named '" + req.getName() + "' already exists");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Wishlist wishlist = new Wishlist();
        wishlist.setUser(user);
        wishlist.setName(req.getName());
        wishlist.setPublic(req.isPublic());

        return toResponse(wishlistRepository.save(wishlist));
    }

    @Override
    @Transactional
    public WishlistResponse updateWishlist(long userId, long wishlistId, UpdateWishlistRequest req) {
        Wishlist wishlist = findOwned(userId, wishlistId);

        if (req.getName() != null && !req.getName().equals(wishlist.getName())) {
            if (wishlistRepository.existsByNameAndUserId(req.getName(), userId)) {
                throw new ConflictException("A wishlist named '" + req.getName() + "' already exists");
            }
            wishlist.setName(req.getName());
        }
        if (req.getIsPublic() != null) {
            wishlist.setPublic(req.getIsPublic());
        }

        return toResponse(wishlistRepository.save(wishlist));
    }

    @Override
    @Transactional
    public void deleteWishlist(long userId, long wishlistId) {
        wishlistRepository.delete(findOwned(userId, wishlistId));
    }

    @Override
    @Transactional
    public WishlistItemResponse addItem(long userId, long wishlistId, AddWishlistItemRequest req) {
        Wishlist wishlist = findOwned(userId, wishlistId);

        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + req.getProductId()));

        ProductVariant variant = null;
        if (req.getVariantId() != null) {
            variant = product.getVariants().stream()
                    .filter(v -> v.getId().equals(req.getVariantId()))
                    .findFirst()
                    .orElseThrow(() -> new BadRequestException(
                            "Variant " + req.getVariantId() + " does not belong to product " + req.getProductId()));
        }

        boolean duplicate = (variant != null)
                ? wishlistItemRepository.existsByWishlistIdAndProductIdAndVariantId(
                        wishlistId, req.getProductId(), variant.getId())
                : wishlistItemRepository.existsByWishlistIdAndProductIdAndVariantIsNull(
                        wishlistId, req.getProductId());

        if (duplicate) {
            throw new ConflictException("This item is already in the wishlist");
        }

        WishlistItem item = new WishlistItem();
        item.setWishlist(wishlist);
        item.setProduct(product);
        item.setVariant(variant);

        WishlistItemResponse response = toItemResponse(wishlistItemRepository.save(item));

        Long marketplaceId = product.getMarketplaceId();
        if (marketplaceId != null) {
            activityEventPublisher.publish(new UserActivityEvent(
                    userId, null, product.getId(), marketplaceId, ActivityType.WISHLIST_ADD, Instant.now()));
        }

        return response;
    }

    @Override
    @Transactional
    public void removeItem(long userId, long wishlistId, long itemId) {
        findOwned(userId, wishlistId); // ownership check
        WishlistItem item = wishlistItemRepository.findByIdAndWishlistId(itemId, wishlistId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Item not found with id: " + itemId));

        // Capture before delete to avoid lazy-loading a detached entity.
        Long productId = item.getProduct() != null ? item.getProduct().getId() : null;
        Long marketplaceId = item.getProduct() != null ? item.getProduct().getMarketplaceId() : null;

        wishlistItemRepository.delete(item);

        if (productId != null && marketplaceId != null) {
            activityEventPublisher.publish(new UserActivityEvent(
                    userId, null, productId, marketplaceId, ActivityType.WISHLIST_REMOVE, Instant.now()));
        }
    }

    // -------------------------------------------------------------------------

    private Wishlist findOwned(long userId, long wishlistId) {
        return wishlistRepository.findByIdAndUserId(wishlistId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Wishlist not found with id: " + wishlistId));
    }

    private WishlistSummaryResponse toSummary(Wishlist w) {
        return new WishlistSummaryResponse(
                w.getId(),
                w.getUser().getId(),
                w.getName(),
                w.isPublic(),
                w.getItems().size(),
                w.getCreatedAt(),
                w.getUpdatedAt()
        );
    }

    private WishlistResponse toResponse(Wishlist w) {
        List<WishlistItemResponse> items = w.getItems().stream()
                .map(this::toItemResponse).toList();
        return new WishlistResponse(
                w.getId(),
                w.getUser().getId(),
                w.getName(),
                w.isPublic(),
                items,
                w.getCreatedAt(),
                w.getUpdatedAt()
        );
    }

    private WishlistItemResponse toItemResponse(WishlistItem i) {
        return new WishlistItemResponse(
                i.getId(),
                i.getProduct().getId(),
                i.getProduct().getName(),
                i.getVariant() != null ? i.getVariant().getId() : null,
                i.getVariant() != null ? i.getVariant().getSku() : null,
                i.getAddedAt()
        );
    }
}
