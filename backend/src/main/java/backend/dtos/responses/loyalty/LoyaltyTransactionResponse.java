package backend.dtos.responses.loyalty;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class LoyaltyTransactionResponse {
    private long id;
    private long accountId;
    private long userId;
    private long companyId;
    private String type;
    private long pointsDelta;
    private long valueCents;
    private Long sourceOrderId;
    private Instant expiresAt;
    private String reason;
    private Instant createdAt;
}
