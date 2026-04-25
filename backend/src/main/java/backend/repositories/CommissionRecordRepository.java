package backend.repositories;

import backend.models.core.CommissionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommissionRecordRepository extends JpaRepository<CommissionRecord, Long> {

    Optional<CommissionRecord> findBySubOrderId(long subOrderId);

    List<CommissionRecord> findAllByVendorId(long vendorId);

    List<CommissionRecord> findAllByMarketplaceId(long marketplaceId);
}
