package backend.dtos.responses.loyalty;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@AllArgsConstructor
public class LoyaltyTierResponse {
    private long id;
    private long companyId;
    private String name;
    private long minPoints;
    private BigDecimal earnMultiplier;
    private String perksJson;
    private String badgeColor;
    private int displayOrder;
    private Instant createdAt;
    private Instant updatedAt;
}
