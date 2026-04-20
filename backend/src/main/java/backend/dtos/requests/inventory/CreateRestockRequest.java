package backend.dtos.requests.inventory;

import backend.annotations.safeRichText.SafeRichText;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CreateRestockRequest {

    @NotNull
    private Long productId;

    private Long variantId;

    private Long locationId;

    @NotNull
    @Min(1)
    private Integer requestedQty;

    private LocalDate expectedArrivalDate;

    @Size(max = 2000)
    @SafeRichText
    private String supplierNote;
}
