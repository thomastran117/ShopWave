package backend.repositories;

import backend.models.core.VendorAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VendorAuditLogRepository extends JpaRepository<VendorAuditLog, Long> {

    Page<VendorAuditLog> findByMarketplaceVendorIdOrderByCreatedAtDesc(long marketplaceVendorId, Pageable pageable);
}
