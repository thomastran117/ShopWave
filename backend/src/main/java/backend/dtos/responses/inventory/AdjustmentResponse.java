package backend.dtos.responses.inventory;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class AdjustmentResponse {
    private Long id;
    private Long productId;
    private String productName;
    private Long adjustedByUserId;
    private int delta;
    private int previousStock;
    private int newStock;
    private String reason;
    private String note;
    private Instant createdAt;
}
