package backend.dtos.responses.credit;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class CreditEntryResponse {
    private Long id;
    private long amountCents;
    private String currency;
    private String type;
    private String reason;
    private Long issuedById;
    private Long sourceTicketId;
    private Long sourceOrderIssueId;
    private Long redeemedOnOrderId;
    private Instant expiresAt;
    private Instant createdAt;
}
