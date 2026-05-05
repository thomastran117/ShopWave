package backend.dtos.responses.product;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductVariantResponse(
        Long id,
        String sku,
        BigDecimal price,
        BigDecimal compareAtPrice,
        Integer stock,
        Integer lowStockThreshold,
        boolean purchasable,
        String option1,
        String option2,
        String option3,
        String variantTitle,
        int displayOrder,
        Instant createdAt,
        Instant updatedAt
) {}
