package backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import backend.models.core.ProductImage;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    List<ProductImage> findAllByProductIdOrderByDisplayOrderAsc(long productId);

    List<ProductImage> findAllByProductIdInOrderByDisplayOrderAsc(Collection<Long> productIds);

    Optional<ProductImage> findByIdAndProductId(long id, long productId);

    int countByProductId(long productId);
}
