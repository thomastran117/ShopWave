package backend.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import backend.models.core.OrderIssue;
import backend.models.enums.OrderIssueState;

import java.util.List;

@Repository
public interface OrderIssueRepository extends JpaRepository<OrderIssue, Long> {

    List<OrderIssue> findAllByOrderId(long orderId);

    @Query("SELECT i FROM OrderIssue i WHERE " +
           "(:state IS NULL OR i.state = :state)")
    Page<OrderIssue> findAllByFilters(
            @Param("state") OrderIssueState state,
            Pageable pageable);
}
