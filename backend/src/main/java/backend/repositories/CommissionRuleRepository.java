package backend.repositories;

import backend.models.core.CommissionRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommissionRuleRepository extends JpaRepository<CommissionRule, Long> {

    List<CommissionRule> findByPolicyIdOrderByPriorityDesc(long policyId);

    void deleteByPolicyId(long policyId);
}
