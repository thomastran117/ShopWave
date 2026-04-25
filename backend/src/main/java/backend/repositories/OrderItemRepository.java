package backend.repositories;

import backend.models.core.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findAllByOrderId(long orderId);

    List<OrderItem> findAllBySubOrderId(long subOrderId);

    @Modifying
    @Query("UPDATE OrderItem i SET i.subOrderId = :subOrderId WHERE i.id IN :itemIds")
    void setSubOrderId(@Param("subOrderId") Long subOrderId, @Param("itemIds") Collection<Long> itemIds);
}
