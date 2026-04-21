package backend.dtos.responses.risk;

import backend.models.enums.RiskReviewStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/** Row shape for the merchant's PENDING/APPROVED/REJECTED risk-review queue. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RiskReviewResponse {
    private Long id;
    private long orderId;
    private Long assessmentId;
    private RiskReviewStatus status;
    private Long decidedByUserId;
    private Instant decidedAt;
    private String merchantNote;
    private Instant createdAt;
    /** Convenience fields denormalized so the queue row is self-sufficient for a list view. */
    private Integer score;
    private String topReason;
}
