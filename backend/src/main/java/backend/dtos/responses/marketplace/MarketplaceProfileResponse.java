package backend.dtos.responses.marketplace;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class MarketplaceProfileResponse {
    private Long id;
    private Long companyId;
    private String companyName;
    private String slug;
    private Long defaultCommissionPolicyId;
    private String payoutSchedule;
    private int holdPeriodDays;
    private String defaultCurrency;
    private boolean acceptingApplications;
    private Instant createdAt;
    private Instant updatedAt;
}
