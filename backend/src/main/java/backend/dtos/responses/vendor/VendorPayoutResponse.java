package backend.dtos.responses.vendor;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@AllArgsConstructor
public class VendorPayoutResponse {
    private Long id;
    private Long vendorId;
    private Long marketplaceId;
    private Instant periodStart;
    private Instant periodEnd;
    private BigDecimal grossAmount;
    private BigDecimal commissionAmount;
    private BigDecimal refundAmount;
    private BigDecimal adjustmentAmount;
    private BigDecimal netAmount;
    private String currency;
    private String status;
    private String stripeTransferId;
    private String failureReason;
    private Instant scheduledAt;
    private Instant paidAt;
    private Instant createdAt;
    private Instant updatedAt;
    private List<VendorPayoutItemResponse> items;
}
