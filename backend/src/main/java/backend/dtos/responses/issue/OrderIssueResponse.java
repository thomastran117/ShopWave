package backend.dtos.responses.issue;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class OrderIssueResponse {
    private Long id;
    private Long orderId;
    private Long ticketId;
    private Long reportedById;
    private String type;
    private String state;
    private String resolution;
    private String description;
    private String rejectionReason;
    private Long returnId;
    private Long replacementOrderId;
    private Long customerCreditId;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant resolvedAt;
}
