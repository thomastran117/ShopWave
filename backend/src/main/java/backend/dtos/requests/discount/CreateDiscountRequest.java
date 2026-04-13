package backend.dtos.requests.discount;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class CreateDiscountRequest {

    @NotBlank
    @Size(max = 255)
    private String name;

    /** "PERCENTAGE" or "FIXED_AMOUNT" — parsed to enum in service with a controlled error message. */
    @NotBlank
    private String type;

    @NotNull
    @DecimalMin("0.01")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal value;

    /** Null = immediately effective. */
    private Instant startDate;

    /** Null = never expires. */
    private Instant endDate;

    @NotNull
    @Size(min = 1)
    private List<Long> productIds;
}
