package backend.dtos.responses.inventory;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class ProductSalesMetricResponse {
    private Long productId;
    private String productName;
    private String sku;
    private Integer currentStock;
    private BigDecimal price;
    private String currency;
    private long totalUnitsSold;
    private BigDecimal totalRevenue;
    /** Current stock quantity multiplied by unit price. Null when stock is untracked. */
    private BigDecimal currentStockValue;
}
