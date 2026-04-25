package backend.repositories;

import backend.models.core.LoyaltyTransaction;
import backend.models.enums.LoyaltyTransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoyaltyTransactionRepository extends JpaRepository<LoyaltyTransaction, Long> {

    Page<LoyaltyTransaction> findByAccountId(long accountId, Pageable pageable);

    Optional<LoyaltyTransaction> findFirstBySourceOrderIdAndType(long sourceOrderId, LoyaltyTransactionType type);

    List<LoyaltyTransaction> findByUserIdAndCompanyId(long userId, long companyId);

    /** Earn transactions that have expired and haven't been offset by an EXPIRE entry yet. */
    @Query("SELECT t FROM LoyaltyTransaction t WHERE t.account.id = :accountId " +
           "AND t.type IN ('EARN_ORDER', 'EARN_BONUS', 'EARN_BIRTHDAY') " +
           "AND t.expiresAt IS NOT NULL AND t.expiresAt < :now")
    List<LoyaltyTransaction> findExpiredEarns(@Param("accountId") long accountId, @Param("now") Instant now);

    /** Check whether a birthday reward has already been given this calendar year. */
    @Query("SELECT COUNT(t) > 0 FROM LoyaltyTransaction t WHERE t.account.id = :accountId " +
           "AND t.type = 'EARN_BIRTHDAY' AND YEAR(t.createdAt) = :year")
    boolean existsBirthdayRewardForYear(@Param("accountId") long accountId, @Param("year") int year);

    /** Accounts whose policy has expiry and that have at least one positive balance — candidates for expiry run. */
    @Query(value =
           "SELECT DISTINCT t.account_id FROM loyalty_transactions t " +
           "JOIN loyalty_accounts a ON a.id = t.account_id " +
           "WHERE t.type IN ('EARN_ORDER','EARN_BONUS','EARN_BIRTHDAY') " +
           "AND t.expires_at IS NOT NULL AND t.expires_at < :now " +
           "AND a.points_balance > 0",
           nativeQuery = true)
    List<Long> findAccountIdsWithExpiredPoints(@Param("now") Instant now);
}
