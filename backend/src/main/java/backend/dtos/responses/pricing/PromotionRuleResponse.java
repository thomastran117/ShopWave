package backend.dtos.responses.pricing;

import com.fasterxml.jackson.databind.JsonNode;

import backend.models.enums.DiscountStatus;
import backend.models.enums.PromotionRuleType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record PromotionRuleResponse(
        Long id,
        Long companyId,
        String name,
        String description,
        PromotionRuleType ruleType,
        JsonNode config,
        DiscountStatus status,
        int priority,
        boolean stackable,
        Instant startDate,
        Instant endDate,
        BigDecimal minCartAmount,
        Integer maxUses,
        int usedCount,
        Integer maxUsesPerUser,
        Long fundedByCompanyId,
        List<Long> targetProductIds,
        List<Long> targetBundleIds,
        List<Long> targetSegmentIds,
        Instant createdAt,
        Instant updatedAt
) {}
