package backend.repositories;

import backend.models.core.SavedPaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedPaymentMethodRepository extends JpaRepository<SavedPaymentMethod, Long> {

    List<SavedPaymentMethod> findAllByUserId(Long userId);

    Optional<SavedPaymentMethod> findByStripePaymentMethodId(String stripePaymentMethodId);

    Optional<SavedPaymentMethod> findByUserIdAndIsDefaultTrue(Long userId);
}
