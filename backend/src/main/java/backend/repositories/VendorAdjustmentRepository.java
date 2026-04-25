package backend.repositories;

import backend.models.core.VendorAdjustment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VendorAdjustmentRepository extends JpaRepository<VendorAdjustment, Long> {

    List<VendorAdjustment> findAllByVendorIdAndAppliedToPayoutIdIsNull(long vendorId);

    List<VendorAdjustment> findAllByVendorId(long vendorId);
}
