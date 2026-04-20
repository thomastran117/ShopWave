package backend.dtos.requests.inventory;

import backend.annotations.safeRichText.SafeRichText;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import backend.models.enums.AdjustmentReason;

@Getter
@Setter
public class BulkAdjustItem {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Delta is required")
    private Integer delta;

    @NotNull(message = "Reason is required")
    private AdjustmentReason reason;

    @Size(max = 500, message = "Note must not exceed 500 characters")
    @SafeRichText
    private String note;
}
