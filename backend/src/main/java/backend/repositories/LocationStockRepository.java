package backend.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import backend.models.core.LocationStock;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocationStockRepository extends JpaRepository<LocationStock, Long> {

    List<LocationStock> findAllByLocationId(long locationId);

    List<LocationStock> findAllByProductId(long productId);

    Optional<LocationStock> findByLocationIdAndProductIdAndVariantRef(
            long locationId, long productId, long variantRef);

    @Query("SELECT ls FROM LocationStock ls WHERE ls.product.id = :productId " +
           "AND ls.location.company.id = :companyId ORDER BY ls.location.displayOrder ASC, ls.location.name ASC")
    List<LocationStock> findAllByProductIdAndCompanyId(
            @Param("productId") long productId, @Param("companyId") long companyId);

    /**
     * Picks the best-stocked active location for a product (product-level stock).
     * JOIN FETCH avoids a lazy-load inside the order transaction.
     */
    @Query("SELECT ls FROM LocationStock ls JOIN FETCH ls.location loc " +
           "WHERE ls.product.id = :productId AND ls.variantRef = 0 " +
           "AND loc.active = true AND ls.stock > 0 ORDER BY ls.stock DESC")
    List<LocationStock> findTopByProductStockDesc(@Param("productId") long productId, Pageable pageable);

    /**
     * Picks the best-stocked active location for a specific variant.
     * JOIN FETCH avoids a lazy-load inside the order transaction.
     */
    @Query("SELECT ls FROM LocationStock ls JOIN FETCH ls.location loc " +
           "WHERE ls.product.id = :productId AND ls.variantRef = :variantRef " +
           "AND loc.active = true AND ls.stock > 0 ORDER BY ls.stock DESC")
    List<LocationStock> findTopByVariantStockDesc(
            @Param("productId") long productId,
            @Param("variantRef") long variantRef,
            Pageable pageable);

    /** Used to guard against deleting a location that still has stock records. */
    boolean existsByLocationId(long locationId);

    /**
     * Atomically decrements stock. Returns 1 on success (stock >= qty), 0 on failure.
     */
    @Modifying
    @Query("UPDATE LocationStock ls SET ls.stock = ls.stock - :qty " +
           "WHERE ls.id = :id AND ls.stock >= :qty")
    int decrementStock(@Param("id") long id, @Param("qty") int qty);

    /**
     * Unconditional restore — used in cancel/compensation flows.
     */
    @Modifying
    @Query("UPDATE LocationStock ls SET ls.stock = ls.stock + :qty WHERE ls.id = :id")
    int restoreStock(@Param("id") long id, @Param("qty") int qty);

    /**
     * Signed delta adjustment with negative-stock guard.
     * Returns 1 on success, 0 if result would be negative.
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE LocationStock ls SET ls.stock = ls.stock + :delta " +
           "WHERE ls.id = :id AND (ls.stock + :delta) >= 0")
    int adjustStock(@Param("id") long id, @Param("delta") int delta);

    /**
     * Direct set — used by setLocationStock to replace stock and threshold entirely.
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE LocationStock ls SET ls.stock = :stock, ls.lowStockThreshold = :threshold " +
           "WHERE ls.id = :id")
    int setStock(@Param("id") long id, @Param("stock") int stock,
                 @Param("threshold") Integer threshold);
}
