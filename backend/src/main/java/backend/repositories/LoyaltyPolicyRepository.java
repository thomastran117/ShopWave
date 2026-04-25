package backend.repositories;

import backend.models.core.LoyaltyPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoyaltyPolicyRepository extends JpaRepository<LoyaltyPolicy, Long> {

    Optional<LoyaltyPolicy> findFirstByCompanyIdAndActiveTrue(long companyId);

    List<LoyaltyPolicy> findByCompanyId(long companyId);

    List<LoyaltyPolicy> findAllByActiveTrue();
}
