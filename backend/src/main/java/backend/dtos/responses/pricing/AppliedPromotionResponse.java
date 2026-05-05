package backend.dtos.responses.pricing;

import backend.models.enums.PromotionRuleType;

import java.math.BigDecimal;

public record AppliedPromotionResponse(
        long ruleId,
        String name,
        PromotionRuleType ruleType,
        BigDecimal savings,
        long fundedByCompanyId
) {}
