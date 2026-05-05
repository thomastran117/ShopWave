package backend.repositories;

import backend.models.core.CustomerAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerAddressRepository extends JpaRepository<CustomerAddress, Long> {

    List<CustomerAddress> findAllByUserIdOrderByIsDefaultDescCreatedAtAsc(long userId);

    Optional<CustomerAddress> findByIdAndUserId(long id, long userId);

    boolean existsByIdAndUserId(long id, long userId);

    Optional<CustomerAddress> findByUserIdAndIsDefaultTrue(long userId);

    @Modifying
    @Query("UPDATE CustomerAddress a SET a.isDefault = false WHERE a.user.id = :userId")
    void clearDefaultForUser(@Param("userId") long userId);
}
