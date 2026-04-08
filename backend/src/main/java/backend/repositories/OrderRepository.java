package backend.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import backend.models.core.Order;
import backend.models.enums.OrderStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findAllByUserId(long userId, Pageable pageable);
    Optional<Order> findByIdAndUserId(long id, long userId);
    Optional<Order> findByPaymentIntentId(String paymentIntentId);
    Page<Order> findAllByUserIdAndStatus(long userId, OrderStatus status, Pageable pageable);
    List<Order> findAllByStatusAndCompensatedFalseAndCreatedAtBefore(OrderStatus status, Instant before);

    @Query("SELECT DISTINCT o FROM Order o JOIN o.items oi WHERE oi.product.company.id = :companyId")
    Page<Order> findAllByProductCompanyId(@Param("companyId") long companyId, Pageable pageable);

    @Query("SELECT DISTINCT o FROM Order o JOIN o.items oi WHERE oi.product.company.id = :companyId AND o.status = :status")
    Page<Order> findAllByProductCompanyIdAndStatus(@Param("companyId") long companyId, @Param("status") OrderStatus status, Pageable pageable);

    @Query("SELECT o FROM Order o JOIN o.items oi WHERE o.id = :orderId AND oi.product.company.id = :companyId")
    Optional<Order> findByIdAndProductCompanyId(@Param("orderId") long orderId, @Param("companyId") long companyId);
}
