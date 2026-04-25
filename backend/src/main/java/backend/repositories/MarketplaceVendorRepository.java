package backend.repositories;

import backend.models.core.MarketplaceVendor;
import backend.models.enums.VendorStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MarketplaceVendorRepository extends JpaRepository<MarketplaceVendor, Long> {

    Optional<MarketplaceVendor> findByMarketplaceIdAndVendorCompanyId(long marketplaceId, long vendorCompanyId);

    Optional<MarketplaceVendor> findByIdAndMarketplaceId(long id, long marketplaceId);

    Page<MarketplaceVendor> findByMarketplaceId(long marketplaceId, Pageable pageable);

    Page<MarketplaceVendor> findByMarketplaceIdAndStatus(long marketplaceId, VendorStatus status, Pageable pageable);

    boolean existsByMarketplaceIdAndVendorCompanyId(long marketplaceId, long vendorCompanyId);

    Optional<MarketplaceVendor> findByStripeConnectAccountId(String stripeConnectAccountId);
}
