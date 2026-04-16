package backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import backend.models.core.Return;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReturnRepository extends JpaRepository<Return, Long> {

    @Query("SELECT r FROM Return r LEFT JOIN FETCH r.evidenceUrls WHERE r.order.id = :orderId")
    List<Return> findAllByOrderId(@Param("orderId") long orderId);

    /** Used to correlate Stripe charge.refunded / refund.updated webhook events back to a Return. */
    Optional<Return> findByStripeRefundId(String stripeRefundId);

    /** Returns all non-terminal return requests for an order (excludes REJECTED and FAILED). */
    @Query("SELECT r FROM Return r LEFT JOIN FETCH r.evidenceUrls " +
           "WHERE r.order.id = :orderId AND r.status NOT IN ('REJECTED','FAILED')")
    List<Return> findActiveByOrderId(@Param("orderId") long orderId);

    /** All returns for orders belonging to a company — used for merchant return management views. */
    @Query("SELECT DISTINCT r FROM Return r LEFT JOIN FETCH r.evidenceUrls " +
           "JOIN r.items ri JOIN ri.orderItem oi WHERE oi.product.company.id = :companyId")
    List<Return> findAllByCompanyId(@Param("companyId") long companyId);

    /** Returns for a specific order scoped to items belonging to a company. */
    @Query("SELECT DISTINCT r FROM Return r LEFT JOIN FETCH r.evidenceUrls " +
           "JOIN r.items ri JOIN ri.orderItem oi " +
           "WHERE r.order.id = :orderId AND oi.product.company.id = :companyId")
    List<Return> findAllByOrderIdAndCompanyId(@Param("orderId") long orderId, @Param("companyId") long companyId);

    /** Single return scoped to items belonging to a company — prevents cross-company access. */
    @Query("SELECT DISTINCT r FROM Return r LEFT JOIN FETCH r.evidenceUrls " +
           "JOIN r.items ri JOIN ri.orderItem oi " +
           "WHERE r.id = :returnId AND oi.product.company.id = :companyId")
    Optional<Return> findByIdAndCompanyId(@Param("returnId") long returnId, @Param("companyId") long companyId);
}
