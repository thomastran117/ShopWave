package backend.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import backend.models.core.PromotionRule;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionRuleRepository extends JpaRepository<PromotionRule, Long> {

    Optional<PromotionRule> findByIdAndCompanyId(long id, long companyId);

    Page<PromotionRule> findAllByCompanyId(long companyId, Pageable pageable);

    Optional<PromotionRule> findByLegacyDiscountId(long legacyDiscountId);

    boolean existsByLegacyDiscountIdIsNotNull();

    /**
     * Candidate rules for PricingEngine: ACTIVE, within time window, and either owned by one
     * of the cart's companies or marketplace-funded for that company.
     * Product-set and segment-set filtering is applied in the engine (cheaper in-memory with
     * pre-fetched associations than a complex SQL join).
     */
    @Query("""
            SELECT DISTINCT r FROM PromotionRule r
            WHERE r.company.id IN :companyIds
              AND r.status = backend.models.enums.DiscountStatus.ACTIVE
              AND (:now >= r.startDate OR r.startDate IS NULL)
              AND (:now <  r.endDate   OR r.endDate   IS NULL)
              AND (r.maxUses IS NULL OR r.usedCount < r.maxUses)
            """)
    List<PromotionRule> findActiveCandidates(
            @Param("companyIds") Collection<Long> companyIds,
            @Param("now") Instant now);

    /**
     * Atomic increment. Returns 1 on success, 0 if the total-uses cap has been reached.
     * Callers must fail-and-rollback on a 0 response.
     */
    @Modifying
    @Query("UPDATE PromotionRule r SET r.usedCount = r.usedCount + 1 WHERE r.id = :id AND (r.maxUses IS NULL OR r.usedCount < r.maxUses)")
    int tryIncrementUsedCount(@Param("id") long id);

    /** Bulk-deletes expired rules. Called by an expiry scheduler analogous to DiscountExpiryScheduler. */
    @Modifying
    @Query("DELETE FROM PromotionRule r WHERE r.endDate IS NOT NULL AND r.endDate < :now")
    int deleteAllExpiredBefore(@Param("now") Instant now);

    /** Cleanup when a product is deleted. */
    @Modifying
    @Query(value = "DELETE FROM promotion_rule_products WHERE product_id = :productId", nativeQuery = true)
    void removeProductFromAllRules(@Param("productId") long productId);

    @Modifying
    @Query(value = "DELETE FROM promotion_rule_products WHERE product_id IN :productIds", nativeQuery = true)
    void removeProductsFromAllRules(@Param("productIds") List<Long> productIds);
}
