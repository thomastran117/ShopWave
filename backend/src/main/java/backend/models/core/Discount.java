package backend.models.core;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import backend.models.enums.DiscountStatus;
import backend.models.enums.DiscountType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "discounts", indexes = {
        @Index(name = "idx_discount_company",  columnList = "company_id"),
        @Index(name = "idx_discount_status",   columnList = "status"),
        @Index(name = "idx_discount_end_date", columnList = "end_date")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class Discount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false, length = 255)
    private String name;

    /**
     * Optional thematic label for the discount (e.g. "summer", "school", "weekly").
     * Used to group and filter discounts and their associated products in search.
     */
    @Column(nullable = true, length = 100)
    private String discountCategory;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DiscountType type;

    /** Percentage (0 < value ≤ 100) for PERCENTAGE; absolute amount for FIXED_AMOUNT. */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal value;

    /** Stored status: ACTIVE or DISABLED only. EXPIRED is computed at response time. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DiscountStatus status = DiscountStatus.ACTIVE;

    /** Null = immediately effective. */
    @Column(nullable = true)
    private Instant startDate;

    /** Null = never expires. When non-null and in the past, treated as EXPIRED in responses. */
    @Column(nullable = true)
    private Instant endDate;

    /**
     * Explicit product set this discount applies to.
     * No cascade — deleting a discount does not delete products.
     * Deleting a product removes join-table rows via DiscountRepository cleanup queries.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "discount_products",
            joinColumns = @JoinColumn(name = "discount_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    @BatchSize(size = 50)
    private Set<Product> products = new HashSet<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;
}
