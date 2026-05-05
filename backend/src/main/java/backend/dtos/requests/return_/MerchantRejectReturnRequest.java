package backend.dtos.requests.return_;

import jakarta.validation.constraints.NotBlank;

public record MerchantRejectReturnRequest(
        @NotBlank String merchantNote
) {}
