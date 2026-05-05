package backend.repositories;

import backend.models.core.VendorPayoutItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VendorPayoutItemRepository extends JpaRepository<VendorPayoutItem, Long> {

    List<VendorPayoutItem> findAllByPayoutId(long payoutId);
}
