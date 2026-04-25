package backend.repositories;

import backend.models.core.MarketplaceProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MarketplaceProfileRepository extends JpaRepository<MarketplaceProfile, Long> {

    Optional<MarketplaceProfile> findByCompanyId(long companyId);

    Optional<MarketplaceProfile> findBySlug(String slug);

    boolean existsByCompanyId(long companyId);

    boolean existsBySlug(String slug);
}
