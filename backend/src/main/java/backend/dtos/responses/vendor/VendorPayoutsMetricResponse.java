package backend.dtos.responses.vendor;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@AllArgsConstructor
public class VendorPayoutsMetricResponse {
    private long vendorId;
    private BigDecimal totalPaidOut;
    private long totalPayouts;
    private List<PayoutSummary> recent;

    public record PayoutSummary(
            long payoutId,
            BigDecimal netAmount,
            String currency,
            String status,
            Instant paidAt
    ) {}
}
