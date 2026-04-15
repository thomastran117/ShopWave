package backend.models.core;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import backend.models.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orders", indexes = {
        @Index(name = "idx_order_user", columnList = "user_id"),
        @Index(name = "idx_order_payment_intent", columnList = "payment_intent_id")
})
@EntityListeners(AuditingEntityListener.class)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal totalAmount;

    /** FK to the coupon applied at checkout. Null if no coupon was used. */
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "coupon_id", nullable = true)
    private Coupon coupon;

    /** Snapshot of the coupon code at order time. Null if no coupon was used. */
    @Column(nullable = true, length = 50)
    private String couponCode;

    /** Amount deducted from the pre-coupon total. Zero if no coupon was applied. */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal couponDiscountAmount = BigDecimal.ZERO;

    @Column(nullable = false, length = 3)
    private String currency = "USD";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private OrderStatus status = OrderStatus.RESERVED;

    @Column(nullable = true, length = 255)
    private String paymentIntentId;

    @Column(nullable = true, length = 255)
    private String paymentClientSecret;

    @Column(nullable = true, length = 500)
    private String failureReason;

    @Column(nullable = false)
    private boolean compensated = false;

    // -------------------------------------------------------------------------
    // Fulfillment tracking
    // -------------------------------------------------------------------------

    /** Carrier tracking number (set when order transitions to SHIPPED). */
    @Column(nullable = true, length = 100)
    private String trackingNumber;

    /** Carrier name (e.g. "UPS", "FedEx"). Set alongside trackingNumber. */
    @Column(nullable = true, length = 60)
    private String carrier;

    /** Timestamp when all items were handed to the carrier. */
    @Column(nullable = true)
    private Instant shippedAt;

    /** Timestamp when delivery was confirmed. */
    @Column(nullable = true)
    private Instant deliveredAt;

    /** Timestamp when items were returned by the customer. */
    @Column(nullable = true)
    private Instant returnedAt;

    /** Optional note added by the merchant during fulfillment actions. */
    @Column(nullable = true, length = 500)
    private String fulfillmentNote;

    // -------------------------------------------------------------------------
    // Refund tracking
    // -------------------------------------------------------------------------

    /** Cumulative amount refunded across all Return records, in cents. Denormalized for fast status checks. */
    @Column(nullable = false)
    private long refundedAmountCents = 0L;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    private List<Return> returns = new ArrayList<>();

    // -------------------------------------------------------------------------
    // Audit
    // -------------------------------------------------------------------------

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;
}
