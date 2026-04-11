package backend.models.core;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import org.hibernate.annotations.BatchSize;

import backend.models.enums.ProductStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "products", indexes = {
        @Index(name = "idx_product_company", columnList = "company_id"),
        @Index(name = "idx_product_sku_company", columnList = "sku, company_id", unique = true)
})
@EntityListeners(AuditingEntityListener.class)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = true, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = true, length = 100)
    private String sku;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = true, precision = 12, scale = 2)
    private BigDecimal compareAtPrice;

    @Column(nullable = false, length = 3)
    private String currency = "USD";

    @Column(nullable = true, length = 100)
    private String category;

    @Column(nullable = true, length = 100)
    private String brand;

    @Column(nullable = true, columnDefinition = "TEXT")
    private String tags;

    @Column(nullable = true, length = 500)
    private String thumbnailUrl;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @BatchSize(size = 50)
    @OrderBy("displayOrder ASC")
    private List<ProductImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @BatchSize(size = 50)
    @OrderBy("position ASC")
    private List<ProductOption> options = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @BatchSize(size = 50)
    @OrderBy("displayOrder ASC")
    private List<ProductVariant> variants = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @BatchSize(size = 50)
    @OrderBy("displayOrder ASC")
    private List<ProductAttribute> attributes = new ArrayList<>();

    @Column(nullable = true)
    private Integer stock;

    @Column(nullable = true)
    private Integer lowStockThreshold;

    /** Alert when stock falls to this percentage of maxStock (0–100). Null = no percent threshold. */
    @Column(nullable = true)
    private Integer lowStockThresholdPercent;

    /** Maximum / initial stock capacity. Used as denominator for percent threshold calculation. */
    @Column(nullable = true)
    private Integer maxStock;

    @Column(nullable = true, precision = 10, scale = 3)
    private BigDecimal weight;

    @Column(nullable = true, length = 10)
    private String weightUnit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductStatus status = ProductStatus.DRAFT;

    @Column(nullable = false)
    private boolean featured = false;

    @Column(nullable = false)
    private boolean purchasable = true;

    @Column(nullable = false)
    private boolean backorderEnabled = false;

    @Column(nullable = false)
    private boolean listed = true;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;
}
