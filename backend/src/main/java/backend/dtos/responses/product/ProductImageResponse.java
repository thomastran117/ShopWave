package backend.dtos.responses.product;

import java.time.Instant;

public record ProductImageResponse(
        Long id,
        String imageUrl,
        int displayOrder,
        Instant createdAt
) {}
