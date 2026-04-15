package backend.models.core;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import backend.models.enums.RefundStatus;
import backend.models.enums.ReturnReason;
import backend.models.enums.ReturnStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "returns", indexes = {
        @Index(name = "idx_return_order", columnList = "order_id"),
        @Index(name = "idx_return_stripe_refund", columnList = "stripe_refund_id")
})
@EntityListeners(AuditingEntityListener.class)
public class Return {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    /** Null for merchant-initiated returns. Non-null for buyer-initiated requests. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by_user_id")
    private User requestedBy;

    @OneToMany(mappedBy = "returnRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReturnItem> items = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReturnStatus status = ReturnStatus.REQUESTED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true, length = 30)
    private ReturnReason reason;

    /** Buyer-supplied description of the return reason. */
    @Column(nullable = true, length = 1000)
    private String buyerNote;

    /** Merchant note recorded at approval or rejection time. */
    @Column(nullable = true, length = 500)
    private String merchantNote;

    /** Whether the merchant elected to restock returned items. */
    @Column(nullable = false)
    private boolean restockItems = false;

    // -------------------------------------------------------------------------
    // Refund tracking
    // -------------------------------------------------------------------------

    /** Refund amount in cents. Null until a refund is attempted. Zero means intentionally waived. */
    @Column(nullable = true)
    private Long refundedAmountCents;

    /** Stripe refund ID (re_...). Used to correlate charge.refunded / refund.updated webhook events. */
    @Column(nullable = true, length = 255)
    private String stripeRefundId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RefundStatus refundStatus = RefundStatus.NONE;

    @Column(nullable = true, length = 500)
    private String refundFailureReason;

    // -------------------------------------------------------------------------
    // Timestamps
    // -------------------------------------------------------------------------

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    @Column(nullable = true)
    private Instant approvedAt;

    @Column(nullable = true)
    private Instant completedAt;
}
