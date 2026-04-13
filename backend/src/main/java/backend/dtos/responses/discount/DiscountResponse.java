package backend.dtos.responses.discount;

import backend.models.enums.DiscountStatus;
import backend.models.enums.DiscountType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record DiscountResponse(
        Long id,
        Long companyId,
        String name,
        String discountCategory,
        DiscountType type,
        BigDecimal value,
        DiscountStatus status,
        Instant startDate,
        Instant endDate,
        List<Long> productIds,
        Instant createdAt,
        Instant updatedAt
) {}
