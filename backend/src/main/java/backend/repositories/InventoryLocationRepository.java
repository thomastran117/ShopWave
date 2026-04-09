package backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import backend.models.core.InventoryLocation;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryLocationRepository extends JpaRepository<InventoryLocation, Long> {

    List<InventoryLocation> findAllByCompanyIdOrderByDisplayOrderAscNameAsc(long companyId);

    Optional<InventoryLocation> findByIdAndCompanyId(long id, long companyId);

    boolean existsByIdAndCompanyId(long id, long companyId);

    boolean existsByCodeAndCompanyId(String code, long companyId);

    boolean existsByCodeAndCompanyIdAndIdNot(String code, long companyId, long excludeId);
}
