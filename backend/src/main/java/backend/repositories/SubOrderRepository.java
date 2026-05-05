package backend.repositories;

import backend.models.core.SubOrder;
import backend.models.enums.SubOrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubOrderRepository extends JpaRepository<SubOrder, Long> {

    List<SubOrder> findAllByOrderId(long orderId);

    Page<SubOrder> findByMarketplaceVendorId(long marketplaceVendorId, Pageable pageable);

    Page<SubOrder> findByMarketplaceVendorIdAndStatus(long marketplaceVendorId, SubOrderStatus status, Pageable pageable);

    Optional<SubOrder> findByIdAndMarketplaceVendorId(long id, long marketplaceVendorId);

    @Query("SELECT s FROM SubOrder s WHERE s.order.id = :orderId AND s.marketplaceVendor.id = :vendorId")
    Optional<SubOrder> findByOrderIdAndMarketplaceVendorId(
            @Param("orderId") long orderId,
            @Param("vendorId") long vendorId);
}
