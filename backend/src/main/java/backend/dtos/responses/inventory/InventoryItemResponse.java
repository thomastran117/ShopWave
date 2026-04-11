package backend.dtos.responses.inventory;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@AllArgsConstructor
public class InventoryItemResponse {
    private Long productId;
    private String productName;
    private String sku;
    private Integer stock;
    private Integer lowStockThreshold;
    private Integer lowStockThresholdPercent;
    private Integer maxStock;
    private boolean lowStock;
    private boolean outOfStock;
    private String stockStatus;
    private BigDecimal price;
    private String currency;
    private Instant updatedAt;
}
