package backend.repositories;

import backend.models.core.ProductBundle;
import backend.models.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BundleRepository extends JpaRepository<ProductBundle, Long> {
    Optional<ProductBundle> findByIdAndCompanyId(long id, long companyId);
    List<ProductBundle> findAllByIdInAndCompanyId(List<Long> ids, long companyId);
    List<ProductBundle> findAllByCompanyId(long companyId);
    Page<ProductBundle> findAllByCompanyId(long companyId, Pageable pageable);
    Page<ProductBundle> findAllByCompanyIdAndStatus(long companyId, ProductStatus status, Pageable pageable);
    boolean existsByItemsProductId(long productId);
}
