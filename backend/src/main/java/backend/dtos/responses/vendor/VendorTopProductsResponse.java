package backend.dtos.responses.vendor;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor
public class VendorTopProductsResponse {
    private long vendorId;
    private int windowDays;
    private List<ProductEntry> products;

    public record ProductEntry(
            long productId,
            String productName,
            long totalUnitsSold,
            BigDecimal totalRevenue
    ) {}
}
