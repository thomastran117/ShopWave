package backend.dtos.responses.pricing;

import java.math.BigDecimal;
import java.util.List;

public record LineBreakdownResponse(
        int index,
        long productId,
        Long variantId,
        int quantity,
        BigDecimal unitBasePrice,
        BigDecimal savings,
        BigDecimal effectiveLineTotal,
        List<Long> appliedRuleIds
) {}
