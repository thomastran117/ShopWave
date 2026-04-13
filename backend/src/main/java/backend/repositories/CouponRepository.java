package backend.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import backend.models.core.Coupon;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {

    Optional<Coupon> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);

    /** Used when updating a coupon — checks uniqueness against other coupons. */
    boolean existsByCodeIgnoreCaseAndIdNot(String code, long id);

    Optional<Coupon> findByIdAndCompanyId(long id, long companyId);

    Page<Coupon> findAllByCompanyId(long companyId, Pageable pageable);

    /**
     * Atomically increments usedCount only when the coupon is still under its limit.
     * Returns 1 on success, 0 if maxUses has been reached (race condition guard).
     * A return value of 0 should cause the calling transaction to throw ConflictException.
     */
    @Modifying
    @Query("UPDATE Coupon c SET c.usedCount = c.usedCount + 1 WHERE c.id = :id AND (c.maxUses IS NULL OR c.usedCount < c.maxUses)")
    int tryIncrementUsedCount(@Param("id") long id);

    /**
     * Bulk-deletes all coupons whose endDate has passed.
     * Called by CouponExpiryScheduler on a fixed interval.
     */
    @Modifying
    @Query("DELETE FROM Coupon c WHERE c.endDate IS NOT NULL AND c.endDate < :now")
    int deleteAllExpiredBefore(@Param("now") Instant now);
}
