package backend.dtos.requests.return_;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record BuyerReturnItemRequest(
        @NotNull Long orderItemId,
        @Min(1) int quantityToReturn
) {}
