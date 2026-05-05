package backend.dtos.requests.support;

import jakarta.validation.constraints.NotNull;

public record AssignTicketRequest(
        @NotNull Long staffUserId
) {}
