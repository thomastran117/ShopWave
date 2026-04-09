package backend.models.core;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
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

    @Column(nullable = false, length = 255)
    private String productName;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "fulfillment_location_id", nullable = true)
    private InventoryLocation fulfillmentLocation;

    /** Snapshot of the fulfillment location name at the time of order. */
    @Column(nullable = true, length = 255)
    private String fulfillmentLocationName;

    /** True when this item was placed against zero stock (backorder). Cleared on fulfillment. */
    @Column(nullable = false)
    private boolean backorder = false;
}
