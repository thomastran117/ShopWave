package backend.services.intf;

import backend.dtos.requests.pricing.CreatePromotionRuleRequest;
import backend.dtos.requests.pricing.UpdatePromotionRuleRequest;
import backend.dtos.responses.general.PagedResponse;
import backend.dtos.responses.pricing.PromotionRuleResponse;

public interface PromotionRuleService {
    PagedResponse<PromotionRuleResponse> listRules(long companyId, long ownerId, int page, int size);
    PromotionRuleResponse getRule(long companyId, long ruleId, long ownerId);
    PromotionRuleResponse createRule(long companyId, long ownerId, CreatePromotionRuleRequest request);
    PromotionRuleResponse updateRule(long companyId, long ruleId, long ownerId, UpdatePromotionRuleRequest request);
    void deleteRule(long companyId, long ruleId, long ownerId);
}
