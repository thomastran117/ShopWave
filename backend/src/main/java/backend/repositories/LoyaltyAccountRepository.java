package backend.repositories;

import backend.models.core.LoyaltyAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoyaltyAccountRepository extends JpaRepository<LoyaltyAccount, Long> {

    Optional<LoyaltyAccount> findByUserIdAndCompanyId(long userId, long companyId);

    Page<LoyaltyAccount> findByCompanyId(long companyId, Pageable pageable);

    /**
     * Atomically deducts points. Returns 1 on success, 0 if balance would go negative.
     * Caller must verify the return value.
     */
    @Modifying
    @Query("UPDATE LoyaltyAccount a SET a.pointsBalance = a.pointsBalance - :delta " +
           "WHERE a.id = :id AND a.pointsBalance >= :delta")
    int deductPoints(@Param("id") long id, @Param("delta") long delta);

    @Modifying
    @Query("UPDATE LoyaltyAccount a SET a.pointsBalance = a.pointsBalance + :delta, " +
           "a.lifetimePoints = a.lifetimePoints + :delta WHERE a.id = :id")
    void addPoints(@Param("id") long id, @Param("delta") long delta);

    @Modifying
    @Query("UPDATE LoyaltyAccount a SET a.pointsBalance = a.pointsBalance + :delta WHERE a.id = :id")
    void addToBalance(@Param("id") long id, @Param("delta") long delta);
}
