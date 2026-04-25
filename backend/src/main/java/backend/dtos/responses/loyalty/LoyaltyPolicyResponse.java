package backend.dtos.responses.loyalty;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@AllArgsConstructor
public class LoyaltyPolicyResponse {
    private long id;
    private long companyId;
    private String name;
    private BigDecimal earnRatePerDollar;
    private int pointValueCents;
    private int minRedemptionPoints;
    private Integer pointsExpiryDays;
    private int birthdayBonusPoints;
    private int birthdayBonusCreditCents;
    private BigDecimal cashbackRatePercent;
    private String earnMode;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}
