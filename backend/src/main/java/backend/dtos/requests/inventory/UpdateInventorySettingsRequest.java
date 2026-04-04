package backend.dtos.requests.inventory;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateInventorySettingsRequest {

    @Min(value = 0, message = "Low stock threshold must be 0 or greater")
    private Integer lowStockThreshold;
}
