package backend.dtos.responses.segment;

import java.time.Instant;

public record CustomerSegmentResponse(
        Long id,
        String code,
        String name,
        String description,
        Instant createdAt,
        Instant updatedAt
) {}
