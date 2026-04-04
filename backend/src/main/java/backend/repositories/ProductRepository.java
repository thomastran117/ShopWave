package backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import backend.models.core.Product;

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
}
