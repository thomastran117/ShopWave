package backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import backend.models.core.ProductVariant;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    Optional<ProductVariant> findByIdAndProductId(long id, long productId);
    Optional<ProductVariant> findByIdAndProductCompanyId(long id, long companyId);
    List<ProductVariant> findAllByProductIdOrderByDisplayOrderAsc(long productId);
    boolean existsBySkuAndProductCompanyId(String sku, long companyId);
    boolean existsByProductId(long productId);

    /**
     * Atomically decrements variant stock. Returns 1 on success, 0 if stock is tracked but < qty.
     * NULL stock (untracked inventory) always succeeds — the decrement is a no-op (NULL - qty = NULL).
     */
    @Modifying
    @Query("UPDATE ProductVariant v SET v.stock = v.stock - :qty WHERE v.id = :id AND (v.stock IS NULL OR v.stock >= :qty)")
    int decrementStock(@Param("id") long id, @Param("qty") int qty);

    /**
     * Restores variant stock unconditionally (used in compensation/cancel flows).
     */
    @Modifying
    @Query("UPDATE ProductVariant v SET v.stock = v.stock + :qty WHERE v.id = :id")
    int restoreStock(@Param("id") long id, @Param("qty") int qty);

    /**
     * Adjusts variant stock by delta. Returns 1 on success, 0 if result would be negative.
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE ProductVariant v SET v.stock = v.stock + :delta WHERE v.id = :id AND v.stock IS NOT NULL AND (v.stock + :delta) >= 0")
    int adjustStock(@Param("id") long id, @Param("delta") int delta);
}
