package backend.repositories;

import backend.models.core.VendorSLAPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VendorSLAPolicyRepository extends JpaRepository<VendorSLAPolicy, Long> {

    List<VendorSLAPolicy> findByMarketplaceId(long marketplaceId);

    Optional<VendorSLAPolicy> findFirstByMarketplaceIdAndActiveTrue(long marketplaceId);

    List<VendorSLAPolicy> findAllByActiveTrue();
}
