package backend.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import backend.models.core.Discount;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface DiscountRepository extends JpaRepository<Discount, Long> {

    Optional<Discount> findByIdAndCompanyId(long id, long companyId);

    Page<Discount> findAllByCompanyId(long companyId, Pageable pageable);

    /**
     * Returns all ACTIVE discounts whose time window covers {@code now} AND that include
     * the given productId. Used at order-creation time to find applicable discounts.
     * Temporal conditions: startDate <= now (or null), endDate > now (or null).
     */
    @Query("""
            SELECT d FROM Discount d JOIN d.products p
            WHERE d.company.id = :companyId
              AND d.status = backend.models.enums.DiscountStatus.ACTIVE
              AND (:now >= d.startDate OR d.startDate IS NULL)
              AND (:now <  d.endDate   OR d.endDate   IS NULL)
              AND p.id = :productId
            """)
    List<Discount> findActiveDiscountsForProduct(
            @Param("companyId") long companyId,
            @Param("productId") long productId,
            @Param("now") Instant now);

    /**
     * Returns the distinct non-null discountCategory values of all ACTIVE, in-window discounts
     * that include the given product. Used by ProductIndexingService to populate the
     * discountCategories field in Elasticsearch.
     */
    @Query("""
            SELECT DISTINCT d.discountCategory FROM Discount d JOIN d.products p
            WHERE p.id = :productId
              AND d.discountCategory IS NOT NULL
              AND d.status = backend.models.enums.DiscountStatus.ACTIVE
              AND (:now >= d.startDate OR d.startDate IS NULL)
              AND (:now <  d.endDate   OR d.endDate   IS NULL)
            """)
    List<String> findActiveDiscountCategoriesByProductId(
            @Param("productId") long productId,
            @Param("now") Instant now);

    /**
     * Bulk-deletes all discounts whose endDate has passed.
     * Called by DiscountExpiryScheduler on a fixed interval.
     * Returns the number of deleted rows for logging.
     */
    @Modifying
    @Query("DELETE FROM Discount d WHERE d.endDate IS NOT NULL AND d.endDate < :now")
    int deleteAllExpiredBefore(@Param("now") Instant now);

    /** Cleans up join-table rows before a single product delete. */
    @Modifying
    @Query(value = "DELETE FROM discount_products WHERE product_id = :productId", nativeQuery = true)
    void removeProductFromAllDiscounts(@Param("productId") long productId);

    /** Cleans up join-table rows before a batch product delete. */
    @Modifying
    @Query(value = "DELETE FROM discount_products WHERE product_id IN :productIds", nativeQuery = true)
    void removeProductsFromAllDiscounts(@Param("productIds") List<Long> productIds);
}
