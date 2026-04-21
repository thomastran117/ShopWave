package backend.repositories;

import backend.models.core.FailedPaymentAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface FailedPaymentAttemptRepository extends JpaRepository<FailedPaymentAttempt, Long> {

    long countByUserIdAndCreatedAtAfter(long userId, Instant since);

    long countByIpAndCreatedAtAfter(String ip, Instant since);
}
