package backend.services.pricing;

import java.math.BigDecimal;
import java.util.List;

/**
 * Per-line output of the engine. {@code effectiveLineTotal} is what the customer pays for
 * this line after all rule + coupon savings attributed to it.
 *
 * @param index                stable 0-based position matching the request
 * @param productId            product id for the line
 * @param variantId            variant id, if the line targeted a variant
 * @param quantity             units ordered
 * @param unitBasePrice        pre-promotion unit price
 * @param savings              total savings attributed to this line (&ge; 0)
 * @param effectiveLineTotal   quantity * unitBasePrice − savings (never &lt; 0)
 * @param appliedRuleIds       ordered ids of rules that touched this line (deduped)
 */
public record LineBreakdown(
        int index,
        long productId,
        Long variantId,
        int quantity,
        BigDecimal unitBasePrice,
        BigDecimal savings,
        BigDecimal effectiveLineTotal,
        List<Long> appliedRuleIds
) {}
