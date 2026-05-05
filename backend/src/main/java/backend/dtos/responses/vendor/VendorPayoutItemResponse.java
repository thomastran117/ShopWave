package backend.dtos.responses.vendor;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class VendorPayoutItemResponse {
    private Long id;
    private Long subOrderId;
    private Long commissionRecordId;
    private Long adjustmentId;
    private String entryType;
    private BigDecimal grossAmount;
    private BigDecimal commissionAmount;
    private BigDecimal netAmount;
}
