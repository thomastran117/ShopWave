package backend.dtos.responses.return_;

import java.time.Instant;
import java.util.List;

public record ReturnResponse(
        Long id,
        Long orderId,
        Long requestedByUserId,
        String status,
        String reason,
        String buyerNote,
        String merchantNote,
        boolean restockItems,
        List<ReturnItemResponse> items,
        List<String> evidenceUrls,
        String returnShipToAddress,
        String returnShipToCity,
        String returnShipToCountry,
        String returnShipToPostalCode,
        Long refundedAmountCents,
        String refundStatus,
        Instant createdAt,
        Instant updatedAt,
        Instant approvedAt,
        Instant completedAt
) {}
