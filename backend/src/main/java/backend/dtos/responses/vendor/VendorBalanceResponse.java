package backend.dtos.responses.vendor;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class VendorBalanceResponse {
    private Long vendorId;
    private long pendingCents;
    private long availableCents;
    private long inTransitCents;
    private long lifetimeGrossCents;
    private long lifetimeCommissionCents;
    private long lifetimePaidOutCents;
    private String currency;
    private Instant updatedAt;
}
