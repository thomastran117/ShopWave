package backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import backend.models.core.User;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    /** Returns the segment ids a user belongs to. Empty for anonymous/unsegmented users. */
    @Query("SELECT s.id FROM User u JOIN u.segments s WHERE u.id = :userId")
    List<Long> findSegmentIdsByUserId(@Param("userId") long userId);
}
