package backend.repositories;

import backend.models.core.VendorOnboardingDocument;
import backend.models.enums.VendorDocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VendorOnboardingDocumentRepository extends JpaRepository<VendorOnboardingDocument, Long> {

    List<VendorOnboardingDocument> findAllByMarketplaceVendorId(long marketplaceVendorId);

    Optional<VendorOnboardingDocument> findByMarketplaceVendorIdAndDocumentType(
            long marketplaceVendorId, VendorDocumentType documentType);
}
