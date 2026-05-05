package backend.repositories;

import backend.models.core.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    List<Wishlist> findAllByUserIdOrderByCreatedAtDesc(long userId);

    Optional<Wishlist> findByIdAndUserId(long id, long userId);

    boolean existsByNameAndUserId(String name, long userId);
}
