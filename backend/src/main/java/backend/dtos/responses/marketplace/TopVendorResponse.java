package backend.dtos.responses.marketplace;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class TopVendorResponse {
    private long vendorId;
    private String vendorName;
    private long totalSubOrders;
    private BigDecimal totalGrossRevenue;
    private BigDecimal totalCommission;
    private double cancellationRate;
}
