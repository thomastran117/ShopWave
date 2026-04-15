package backend.dtos.requests.return_;

import backend.models.enums.ReturnItemCondition;
import jakarta.validation.constraints.NotNull;

public record InspectReturnItemRequest(
        @NotNull Long returnItemId,
        @NotNull ReturnItemCondition condition,
        boolean restock
) {}
