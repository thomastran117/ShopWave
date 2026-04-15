package backend.dtos.requests.return_;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record InspectReturnRequest(
        @NotEmpty List<@Valid InspectReturnItemRequest> items,
        String merchantNote
) {}
