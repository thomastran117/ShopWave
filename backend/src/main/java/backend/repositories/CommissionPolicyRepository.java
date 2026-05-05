package backend.repositories;

import backend.models.core.CommissionPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface CommissionPolicyRepository extends JpaRepository<CommissionPolicy, Long> {

    List<CommissionPolicy> findByMarketplaceIdAndActiveTrue(long marketplaceId);

    /** Finds the active policy valid at a given point in time. */
    @Query("""
            SELECT p FROM CommissionPolicy p
            WHERE p.marketplaceId = :marketplaceId
              AND p.active = true
              AND (p.effectiveFrom IS NULL OR p.effectiveFrom <= :at)
              AND (p.effectiveTo IS NULL OR p.effectiveTo >= :at)
            ORDER BY p.id DESC
            """)
    List<CommissionPolicy> findActiveAt(@Param("marketplaceId") long marketplaceId, @Param("at") Instant at);
}
