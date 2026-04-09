package backend.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import backend.models.core.RestockRequest;
import backend.models.enums.RestockStatus;

import java.util.Optional;

@Repository
public interface RestockRequestRepository extends JpaRepository<RestockRequest, Long> {

    Page<RestockRequest> findAllByCompanyId(long companyId, Pageable pageable);

    Page<RestockRequest> findAllByCompanyIdAndStatus(long companyId, RestockStatus status, Pageable pageable);

    Page<RestockRequest> findAllByCompanyIdAndProductId(long companyId, long productId, Pageable pageable);

    Page<RestockRequest> findAllByCompanyIdAndStatusAndProductId(long companyId, RestockStatus status, long productId, Pageable pageable);

    Optional<RestockRequest> findByIdAndCompanyId(long id, long companyId);
}
