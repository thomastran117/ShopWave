package backend.dtos.requests.loyalty;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class CreateLoyaltyTierRequest {

    @NotBlank
    private String name;

    @Min(0)
    private long minPoints;

    @NotNull @DecimalMin("0.10") @DecimalMax("10.00")
    private BigDecimal earnMultiplier = BigDecimal.ONE;

    private String perksJson;

    @Size(max = 20)
    private String badgeColor;

    @Min(0)
    private int displayOrder = 0;
}
