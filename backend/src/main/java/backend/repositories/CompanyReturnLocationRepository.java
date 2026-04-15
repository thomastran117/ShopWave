package backend.repositories;

import backend.models.core.CompanyReturnLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CompanyReturnLocationRepository extends JpaRepository<CompanyReturnLocation, Long> {

    List<CompanyReturnLocation> findAllByCompanyId(long companyId);

    Optional<CompanyReturnLocation> findByIdAndCompanyId(long id, long companyId);

    Optional<CompanyReturnLocation> findFirstByCompanyIdAndPrimaryTrue(long companyId);

    long countByCompanyId(long companyId);

    @Modifying
    @Query("UPDATE CompanyReturnLocation l SET l.primary = false WHERE l.company.id = :companyId")
    void clearPrimaryForCompany(@Param("companyId") long companyId);
}
