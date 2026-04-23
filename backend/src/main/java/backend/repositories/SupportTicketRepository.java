package backend.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import backend.models.core.SupportTicket;
import backend.models.enums.TicketStatus;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {

    Page<SupportTicket> findAllByCustomerId(long customerId, Pageable pageable);

    @Query("SELECT t FROM SupportTicket t WHERE " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:assignedToId IS NULL OR t.assignedTo.id = :assignedToId)")
    Page<SupportTicket> findAllByFilters(
            @Param("status") TicketStatus status,
            @Param("assignedToId") Long assignedToId,
            Pageable pageable);

    long countByCustomerIdAndStatusNot(long customerId, TicketStatus status);
}
