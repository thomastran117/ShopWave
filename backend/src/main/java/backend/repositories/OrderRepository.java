package backend.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import backend.models.core.Order;
import backend.models.enums.FulfillmentStatus;
import backend.models.enums.OrderStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findAllByUserId(long userId, Pageable pageable);
    Optional<Order> findByIdAndUserId(long id, long userId);

    /** Total orders placed by this user — feeds CouponAbuseEvaluator's first-order heuristic. */
    long countByUserId(long userId);
    Optional<Order> findByPaymentIntentId(String paymentIntentId);
    Optional<Order> findByStripeInvoiceId(String stripeInvoiceId);
    Page<Order> findAllByUserIdAndStatus(long userId, OrderStatus status, Pageable pageable);
    List<Order> findAllByStatusAndCompensatedFalseAndCreatedAtBefore(OrderStatus status, Instant before);

    @Query("SELECT DISTINCT o FROM Order o JOIN o.items oi WHERE oi.product.company.id = :companyId")
    Page<Order> findAllByProductCompanyId(@Param("companyId") long companyId, Pageable pageable);

    @Query("SELECT DISTINCT o FROM Order o JOIN o.items oi WHERE oi.product.company.id = :companyId AND o.status = :status")
    Page<Order> findAllByProductCompanyIdAndStatus(@Param("companyId") long companyId, @Param("status") OrderStatus status, Pageable pageable);

    @Query("SELECT o FROM Order o JOIN o.items oi WHERE o.id = :orderId AND oi.product.company.id = :companyId")
    Optional<Order> findByIdAndProductCompanyId(@Param("orderId") long orderId, @Param("companyId") long companyId);

    /**
     * Atomically claims compensation rights for an order. Returns 1 if this caller is the
     * first to set compensated=true, 0 if another thread already did so. Callers that
     * receive 0 must skip stock restoration to prevent double-restoring inventory.
     */
    @Modifying
    @Query("UPDATE Order o SET o.compensated = true WHERE o.id = :id AND o.compensated = false")
    int markCompensated(@Param("id") long id);

    /**
     * FIFO: PAID orders that contain at least one BACKORDERED item for the given product.
     * Replaces the retired findBackordersByProductId (which queried BACKORDER-status orders).
     */
    @Query("SELECT DISTINCT o FROM Order o JOIN o.items i " +
           "WHERE o.status = backend.models.enums.OrderStatus.PAID " +
           "AND i.product.id = :productId " +
           "AND i.fulfillmentStatus = :backordered " +
           "ORDER BY o.createdAt ASC")
    List<Order> findPaidOrdersWithBackorderedProduct(
            @Param("productId") long productId,
            @Param("backordered") FulfillmentStatus backordered);

    /**
     * FIFO: PAID orders that contain at least one BACKORDERED item for the given variant.
     * Replaces the retired findBackordersByVariantId.
     */
    @Query("SELECT DISTINCT o FROM Order o JOIN o.items i " +
           "WHERE o.status = backend.models.enums.OrderStatus.PAID " +
           "AND i.variant.id = :variantId " +
           "AND i.fulfillmentStatus = :backordered " +
           "ORDER BY o.createdAt ASC")
    List<Order> findPaidOrdersWithBackorderedVariant(
            @Param("variantId") long variantId,
            @Param("backordered") FulfillmentStatus backordered);

    // -------------------------------------------------------------------------
    // SLA / Operations Dashboard support
    // -------------------------------------------------------------------------

    /**
     * Total distinct orders for the company in the window — denominator for backorder rate.
     * Counts orders by their {@code createdAt} timestamp.
     */
    @Query("SELECT COUNT(DISTINCT o) FROM Order o JOIN o.items i " +
           "WHERE i.product.company.id = :companyId " +
           "AND o.createdAt BETWEEN :from AND :to")
    long countOrdersInWindow(@Param("companyId") long companyId,
                             @Param("from") Instant from, @Param("to") Instant to);

    /**
     * Distinct orders in the window that contained at least one BACKORDERED item — numerator
     * for backorder rate on the SLA dashboard.
     */
    @Query("SELECT COUNT(DISTINCT o) FROM Order o JOIN o.items i " +
           "WHERE i.product.company.id = :companyId " +
           "AND o.createdAt BETWEEN :from AND :to " +
           "AND i.fulfillmentStatus = backend.models.enums.FulfillmentStatus.BACKORDERED")
    long countOrdersWithBackorderedItemsInWindow(@Param("companyId") long companyId,
                                                 @Param("from") Instant from, @Param("to") Instant to);
}
