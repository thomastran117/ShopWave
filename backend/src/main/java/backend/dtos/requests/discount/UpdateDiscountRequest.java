package backend.dtos.requests.discount;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class UpdateDiscountRequest {

    @Size(max = 255)
    private String name;

    /** "PERCENTAGE" or "FIXED_AMOUNT" — parsed to enum in service. Null = no change. */
    private String type;

    @DecimalMin("0.01")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal value;

    /** "ACTIVE" or "DISABLED" only. "EXPIRED" is rejected with 400. Null = no change. */
    private String status;

    /** Null = no change. */
    private Instant startDate;

    /** Null = no change. */
    private Instant endDate;

    /** When provided, fully replaces the product set. Null = no change. */
    private List<Long> productIds;
}
