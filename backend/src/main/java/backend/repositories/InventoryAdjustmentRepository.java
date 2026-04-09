package backend.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import backend.models.core.InventoryAdjustment;

@Repository
public interface InventoryAdjustmentRepository
        extends JpaRepository<InventoryAdjustment, Long>,
                JpaSpecificationExecutor<InventoryAdjustment> {

    Page<InventoryAdjustment> findAllByProductIdAndProductCompanyId(long productId, long companyId, Pageable pageable);
}
