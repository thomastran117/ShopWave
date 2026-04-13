package backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import backend.models.core.CouponRedemption;

@Repository
public interface CouponRedemptionRepository extends JpaRepository<CouponRedemption, Long> {

    /** Used to enforce per-user usage caps. */
    long countByCouponIdAndUserId(long couponId, long userId);
}
