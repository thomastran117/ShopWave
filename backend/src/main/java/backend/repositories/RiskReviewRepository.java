package backend.repositories;

import backend.models.core.RiskReview;
import backend.models.enums.RiskReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RiskReviewRepository extends JpaRepository<RiskReview, Long> {

    Optional<RiskReview> findByOrderId(long orderId);

    /**
     * Paginated queue of reviews for a given company. Joins through the held order's line items
     * so a merchant only sees reviews for orders containing their products — mirrors the pattern
     * used by {@code OrderRepository.findAllByProductCompanyId}.
     */
    @Query("SELECT DISTINCT rr FROM RiskReview rr, Order o JOIN o.items oi " +
           "WHERE rr.orderId = o.id AND oi.product.company.id = :companyId AND rr.status = :status")
    Page<RiskReview> findByCompanyIdAndStatus(
            @Param("companyId") long companyId,
            @Param("status") RiskReviewStatus status,
            Pageable pageable);
}
