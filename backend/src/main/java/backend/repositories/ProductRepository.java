package backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import backend.models.core.Product;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    Optional<Product> findByIdAndCompanyId(long id, long companyId);
    List<Product> findAllByIdInAndCompanyId(Collection<Long> ids, long companyId);
    boolean existsBySkuAndCompanyId(String sku, long companyId);
}
