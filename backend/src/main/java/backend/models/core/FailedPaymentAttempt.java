package backend.models.core;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * Velocity table appended to on every {@code payment_intent.payment_failed} webhook.
 * Read by {@code FailedPaymentVelocityEvaluator}; append-only from the application side.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "failed_payment_attempts", indexes = {
        @Index(name = "idx_fpa_user_created", columnList = "user_id, created_at"),
        @Index(name = "idx_fpa_ip_created", columnList = "ip, created_at")
})
@EntityListeners(AuditingEntityListener.class)
public class FailedPaymentAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    /** Best-effort IP copied from the {@code RiskAssessment} written at checkout. Null if assessment missing. */
    @Column(nullable = true, length = 45)
    private String ip;

    @Column(nullable = true, length = 255)
    private String paymentIntentId;

    @Column(nullable = true, length = 500)
    private String failureReason;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public FailedPaymentAttempt(Long userId, Long orderId, String ip, String paymentIntentId, String failureReason) {
        this.userId = userId;
        this.orderId = orderId;
        this.ip = ip;
        this.paymentIntentId = paymentIntentId;
        this.failureReason = failureReason;
    }
}
