package backend.models.core;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import backend.models.enums.FulfillmentStatus;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "order_items", indexes = {
        @Index(name = "idx_order_item_order", columnList = "order_id"),
        @Index(name = "idx_order_item_fulfillment_loc", columnList = "fulfillment_location_id")
})
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "product_id", nullable = true)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "variant_id", nullable = true)
    private ProductVariant variant;

    @Column(nullable = true, length = 255)
    private String variantTitle;

    @Column(nullable = true, length = 100)
    private String variantSku;

    @Column(nullable = false)
    private int quantity;

    /** Snapshot of the product/variant price at the time of order. */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    /** Per-unit discount applied at order time. Zero if no discount was active. */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(nullable = false, length = 255)
    private String productName;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "fulfillment_location_id", nullable = true)
    private InventoryLocation fulfillmentLocation;

    /** Snapshot of the fulfillment location name at the time of order. */
    @Column(nullable = true, length = 255)
    private String fulfillmentLocationName;

    /**
     * Per-item fulfillment lifecycle status. Starts as PENDING (stock available at order time)
     * or BACKORDERED (zero stock; waiting for restock). Advances through PACKED → SHIPPED →
     * DELIVERED, or RETURNED / CANCELLED on terminal paths.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FulfillmentStatus fulfillmentStatus = FulfillmentStatus.PENDING;

    /** Non-null for bundle order items; null for regular product items. */
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "bundle_id", nullable = true)
    private ProductBundle bundle;

    /** Snapshot of bundle name at order time. */
    @Column(name = "bundle_name", nullable = true, length = 255)
    private String bundleName;
}
