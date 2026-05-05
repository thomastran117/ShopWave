package backend.dtos.responses.support;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@AllArgsConstructor
public class TicketResponse {
    private Long id;
    private Long customerId;
    private String customerName;
    private Long openedById;
    private Long assignedToId;
    private String assignedToName;
    private Long orderId;
    private String subject;
    private String description;
    private String status;
    private String priority;
    private String category;
    private List<TicketMessageResponse> messages;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant resolvedAt;
    private Instant closedAt;
}
