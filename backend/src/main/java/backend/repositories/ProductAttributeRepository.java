package backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import backend.models.core.ProductAttribute;

import java.util.List;

@Repository
public interface ProductAttributeRepository extends JpaRepository<ProductAttribute, Long> {
    List<ProductAttribute> findAllByProductIdOrderByDisplayOrderAsc(long productId);
    void deleteAllByProductId(long productId);
}
