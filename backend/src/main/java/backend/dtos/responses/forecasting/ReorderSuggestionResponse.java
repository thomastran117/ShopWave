package backend.dtos.responses.forecasting;

import java.time.LocalDate;

public record ReorderSuggestionResponse(
        long productId,
        String productName,
        String sku,
        Integer currentStock,
        int suggestedQty,
        ReorderReasonCode reasonCode,
        LocalDate projectedStockoutDate
) {
    public enum ReorderReasonCode {
        BELOW_THRESHOLD,
        STOCKOUT_WITHIN_LEADTIME,
        VELOCITY_SPIKE
    }
}
