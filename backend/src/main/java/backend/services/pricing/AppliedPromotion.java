package backend.services.pricing;

import backend.models.enums.PromotionRuleType;

import java.math.BigDecimal;

/**
 * A rule that fired against the cart, with its net saving and the vendor that funds it.
 * {@code fundedByCompanyId} falls back to the rule's owning company when the rule has no
 * explicit funder set — this lets the order flow snapshot attribution unambiguously.
 */
public record AppliedPromotion(
        long ruleId,
        String name,
        PromotionRuleType ruleType,
        BigDecimal savings,
        long fundedByCompanyId
) {}
