package backend.repositories;

import backend.models.core.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {

    Optional<WishlistItem> findByIdAndWishlistId(long id, long wishlistId);

    /** Duplicate check when variant is specified. */
    boolean existsByWishlistIdAndProductIdAndVariantId(long wishlistId, long productId, long variantId);

    /** Duplicate check when no variant is specified. */
    @Query("""
            SELECT COUNT(i) > 0 FROM WishlistItem i
            WHERE i.wishlist.id = :wishlistId
              AND i.product.id = :productId
              AND i.variant IS NULL
            """)
    boolean existsByWishlistIdAndProductIdAndVariantIsNull(
            @Param("wishlistId") long wishlistId,
            @Param("productId") long productId);
}
