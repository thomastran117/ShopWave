package backend.dtos.requests.return_;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateReturnLocationRequest(
        @NotBlank @Size(max = 255) String address,
        @NotBlank @Size(max = 100) String city,
        @NotBlank @Size(max = 100) String country,
        @Size(max = 20) String postalCode,
        @Size(max = 100) String name,
        boolean primary
) {}
