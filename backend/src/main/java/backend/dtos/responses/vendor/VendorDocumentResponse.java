package backend.dtos.responses.vendor;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class VendorDocumentResponse {
    private Long id;
    private Long marketplaceVendorId;
    private String documentType;
    private String s3Key;
    private Instant uploadedAt;
    private Instant verifiedAt;
    private String rejectionNote;
}
