package backend.dtos.responses.inventory;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class LocationStockResponse {
    private Long id;
    private Long locationId;
    private String locationName;
    private Long productId;
    private Long variantId;          // null when product-level
    private int stock;
    private Integer lowStockThreshold;
    private String stockStatus;      // IN_STOCK | LOW_STOCK | OUT_OF_STOCK
    private Instant updatedAt;
}
