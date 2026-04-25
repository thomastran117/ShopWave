package backend.dtos.responses.vendor;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class MarketplaceVendorResponse {
    private Long id;
    private Long marketplaceId;
    private String marketplaceName;
    private Long vendorCompanyId;
    private String vendorCompanyName;
    private String status;
    private String tier;
    private String onboardingStep;
    private Long commissionPolicyId;
    private String stripeConnectStatus;
    private boolean chargesEnabled;
    private boolean payoutsEnabled;
    private Instant appliedAt;
    private Instant approvedAt;
    private Instant suspendedAt;
    private String rejectionReason;
    private Instant createdAt;
    private Instant updatedAt;
}
