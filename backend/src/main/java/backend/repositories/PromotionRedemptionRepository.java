package backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import backend.models.core.PromotionRedemption;

import java.util.List;

@Repository
public interface PromotionRedemptionRepository extends JpaRepository<PromotionRedemption, Long> {

    List<PromotionRedemption> findAllByOrderId(long orderId);

    List<PromotionRedemption> findAllByRuleId(long ruleId);
}
