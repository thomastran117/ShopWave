package backend.repositories;

import backend.models.core.VendorSLABreach;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VendorSLABreachRepository extends JpaRepository<VendorSLABreach, Long> {

    Page<VendorSLABreach> findByVendorId(long vendorId, Pageable pageable);

    List<VendorSLABreach> findByVendorIdAndResolvedAtIsNull(long vendorId);

    Page<VendorSLABreach> findByPolicyId(long policyId, Pageable pageable);

    long countByVendorIdAndResolvedAtIsNull(long vendorId);
}
