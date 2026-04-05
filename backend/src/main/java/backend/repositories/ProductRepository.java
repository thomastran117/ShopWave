package backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import backend.models.core.Product;
import backend.repositories.projections.ProductSalesProjection;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    Optional<Product> findByIdAndCompanyId(long id, long companyId);
    List<Product> findAllByIdInAndCompanyId(Collection<Long> ids, long companyId);
    boolean existsBySkuAndCompanyId(String sku, long companyId);

    /**
     * Atomically decrements stock. Returns 1 (success) when stock >= quantity, 0 otherwise.
     * The WHERE clause guarantees no oversell at the DB level.
     */
    @Modifying
    @Query("UPDATE Product p SET p.stock = p.stock - :quantity WHERE p.id = :id AND p.stock >= :quantity")
    int decrementStock(@Param("id") long id, @Param("quantity") int quantity);

    /** Restores stock after a failed or cancelled order. */
    @Modifying
    @Query("UPDATE Product p SET p.stock = p.stock + :quantity WHERE p.id = :id")
    int restoreStock(@Param("id") long id, @Param("quantity") int quantity);

    /**
     * Atomically applies a signed delta to stock. Prevents negative stock.
     * Returns 1 on success, 0 if the result would be negative or stock is null.
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Product p SET p.stock = p.stock + :delta WHERE p.id = :id AND p.stock IS NOT NULL AND (p.stock + :delta) >= 0")
    int adjustStock(@Param("id") long id, @Param("delta") int delta);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.company.id = :cid AND p.stock IS NOT NULL AND p.stock = 0")
    long countOutOfStock(@Param("cid") long companyId);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.company.id = :cid AND p.stock IS NOT NULL AND p.lowStockThreshold IS NOT NULL AND p.stock > 0 AND p.stock <= p.lowStockThreshold")
    long countLowStock(@Param("cid") long companyId);

    @Query("SELECT COALESCE(SUM(p.stock * p.price), 0) FROM Product p WHERE p.company.id = :cid AND p.stock IS NOT NULL AND p.stock > 0")
    BigDecimal totalInventoryValue(@Param("cid") long companyId);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.company.id = :cid AND p.stock IS NOT NULL")
    long countTrackedProducts(@Param("cid") long companyId);

    /**
     * Returns the top N products for a company ranked by total units sold across PAID orders.
     * Products with no sales appear at the bottom with totalUnitsSold = 0.
     */
    @Query(nativeQuery = true, value = """
            SELECT
                p.id          AS productId,
                p.name        AS productName,
                p.sku         AS sku,
                p.stock       AS currentStock,
                p.price       AS price,
                p.currency    AS currency,
                COALESCE(SUM(oi.quantity), 0)                    AS totalUnitsSold,
                COALESCE(SUM(oi.quantity * oi.unit_price), 0.00) AS totalRevenue
            FROM products p
            LEFT JOIN order_items oi ON oi.product_id = p.id
            LEFT JOIN orders o ON oi.order_id = o.id AND o.status = 'PAID'
            WHERE p.company_id = :companyId
            GROUP BY p.id, p.name, p.sku, p.stock, p.price, p.currency
            ORDER BY totalUnitsSold DESC
            LIMIT :limit
            """)
    List<ProductSalesProjection> findTopByUnitsSold(
            @Param("companyId") long companyId,
            @Param("limit") int limit);

    /**
     * Returns the top N products for a company ranked by total revenue (unitPrice × quantity)
     * across PAID orders. Uses the price snapshot captured at order time.
     */
    @Query(nativeQuery = true, value = """
            SELECT
                p.id          AS productId,
                p.name        AS productName,
                p.sku         AS sku,
                p.stock       AS currentStock,
                p.price       AS price,
                p.currency    AS currency,
                COALESCE(SUM(oi.quantity), 0)                    AS totalUnitsSold,
                COALESCE(SUM(oi.quantity * oi.unit_price), 0.00) AS totalRevenue
            FROM products p
            LEFT JOIN order_items oi ON oi.product_id = p.id
            LEFT JOIN orders o ON oi.order_id = o.id AND o.status = 'PAID'
            WHERE p.company_id = :companyId
            GROUP BY p.id, p.name, p.sku, p.stock, p.price, p.currency
            ORDER BY totalRevenue DESC
            LIMIT :limit
            """)
    List<ProductSalesProjection> findTopByRevenue(
            @Param("companyId") long companyId,
            @Param("limit") int limit);

    /**
     * Returns products that have stock tracked and available (stock > 0) but have never
     * appeared in a PAID order — potential dead inventory.
     */
    @Query(nativeQuery = true, value = """
            SELECT
                p.id          AS productId,
                p.name        AS productName,
                p.sku         AS sku,
                p.stock       AS currentStock,
                p.price       AS price,
                p.currency    AS currency,
                0             AS totalUnitsSold,
                0.00          AS totalRevenue
            FROM products p
            WHERE p.company_id = :companyId
              AND p.stock IS NOT NULL
              AND p.stock > 0
              AND p.id NOT IN (
                    SELECT DISTINCT oi.product_id
                    FROM order_items oi
                    JOIN orders o ON oi.order_id = o.id
                    WHERE o.status = 'PAID'
              )
            ORDER BY p.stock DESC
            LIMIT :limit
            """)
    List<ProductSalesProjection> findNeverSold(
            @Param("companyId") long companyId,
            @Param("limit") int limit);
}
