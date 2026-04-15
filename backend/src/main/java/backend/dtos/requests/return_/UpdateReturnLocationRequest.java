package backend.dtos.requests.return_;

import jakarta.validation.constraints.Size;

public record UpdateReturnLocationRequest(
        @Size(max = 255) String address,
        @Size(max = 100) String city,
        @Size(max = 100) String country,
        @Size(max = 20) String postalCode,
        @Size(max = 100) String name,
        Boolean primary
) {}
