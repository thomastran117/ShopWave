package backend.dtos.responses.order;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@AllArgsConstructor
public class CommissionRecordResponse {
    private Long id;
    private Long subOrderId;
    private Long vendorId;
    private Long marketplaceId;
    private BigDecimal commissionRate;
    private BigDecimal grossAmount;
    private BigDecimal commissionAmount;
    private BigDecimal netVendorAmount;
    private String currency;
    private Instant computedAt;
}
