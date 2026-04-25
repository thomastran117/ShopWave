package backend.dtos.responses.loyalty;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class LoyaltyAccountResponse {
    private long id;
    private long userId;
    private long companyId;
    private long pointsBalance;
    private long lifetimePoints;
    private Long currentTierId;
    private String currentTierName;
    private Instant tierUpdatedAt;
    private Instant createdAt;
    private Instant updatedAt;
}
