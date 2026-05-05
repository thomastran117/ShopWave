package backend.repositories;

import backend.models.core.VendorSLAMetric;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface VendorSLAMetricRepository extends JpaRepository<VendorSLAMetric, Long> {

    Optional<VendorSLAMetric> findByVendorIdAndDate(long vendorId, LocalDate date);

    List<VendorSLAMetric> findByVendorIdOrderByDateDesc(long vendorId);

    Page<VendorSLAMetric> findByVendorId(long vendorId, Pageable pageable);

    List<VendorSLAMetric> findByVendorIdAndDateBetweenOrderByDateAsc(long vendorId, LocalDate from, LocalDate to);
}
