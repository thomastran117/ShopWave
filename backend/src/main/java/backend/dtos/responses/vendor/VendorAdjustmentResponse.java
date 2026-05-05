package backend.dtos.responses.vendor;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class VendorAdjustmentResponse {
    private Long id;
    private Long vendorId;
    private long amountCents;
    private String currency;
    private String reason;
    private Long createdByUserId;
    private Long appliedToPayoutId;
    private Instant createdAt;
}
