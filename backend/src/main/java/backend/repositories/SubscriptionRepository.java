package backend.repositories;

import backend.models.core.Subscription;
import backend.models.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findByStripeSubscriptionId(String stripeSubscriptionId);

    List<Subscription> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Subscription> findByIdAndUserId(Long id, Long userId);

    List<Subscription> findAllByStatusAndNextBillingAtBetween(
            SubscriptionStatus status, Instant from, Instant to);
}
