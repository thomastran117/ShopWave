package backend.dtos.responses.loyalty;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoyaltyRedemptionQuoteResponse {
    private long userId;
    private long companyId;
    private int pointsToRedeem;
    private long discountCents;
    private long currentBalance;
    private long balanceAfterRedemption;
    private boolean valid;
    private String invalidReason;
}
