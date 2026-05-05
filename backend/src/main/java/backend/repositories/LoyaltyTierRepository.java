package backend.repositories;

import backend.models.core.LoyaltyTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoyaltyTierRepository extends JpaRepository<LoyaltyTier, Long> {

    List<LoyaltyTier> findByCompanyIdOrderByMinPointsAsc(long companyId);

    List<LoyaltyTier> findByCompanyIdOrderByMinPointsDesc(long companyId);
}
