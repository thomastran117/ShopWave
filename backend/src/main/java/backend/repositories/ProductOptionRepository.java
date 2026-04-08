package backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import backend.models.core.ProductOption;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductOptionRepository extends JpaRepository<ProductOption, Long> {
    List<ProductOption> findAllByProductIdOrderByPositionAsc(long productId);
    Optional<ProductOption> findByIdAndProductId(long id, long productId);
    int countByProductId(long productId);
}
