package backend.dtos.responses.return_;

import java.time.Instant;

public record ReturnLocationResponse(
        Long id,
        Long companyId,
        String name,
        String address,
        String city,
        String country,
        String postalCode,
        boolean primary,
        Instant createdAt,
        Instant updatedAt
) {}
