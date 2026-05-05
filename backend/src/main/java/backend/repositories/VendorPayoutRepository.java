package backend.repositories;

import backend.models.core.VendorPayout;
import backend.models.enums.PayoutStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VendorPayoutRepository extends JpaRepository<VendorPayout, Long> {

    Page<VendorPayout> findByVendorId(long vendorId, Pageable pageable);

    Page<VendorPayout> findByVendorIdAndStatus(long vendorId, PayoutStatus status, Pageable pageable);

    Optional<VendorPayout> findByStripeTransferId(String stripeTransferId);

    List<VendorPayout> findAllByStatus(PayoutStatus status);

    Optional<VendorPayout> findByIdAndVendorId(long id, long vendorId);

    @Query("SELECT p FROM VendorPayout p WHERE p.vendorId = :vendorId AND p.status = :status")
    List<VendorPayout> findByVendorIdAndStatusList(
            @Param("vendorId") long vendorId, @Param("status") PayoutStatus status);
}
