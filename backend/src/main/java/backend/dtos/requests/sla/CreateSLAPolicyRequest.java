package backend.dtos.requests.sla;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateSLAPolicyRequest {

    @NotBlank
    private String name;

    @Positive
    private double targetShipHours = 48.0;

    @Positive
    private double targetResponseHours = 24.0;

    @Min(0) @Max(1)
    private double maxCancellationRate = 0.02;

    @Min(0) @Max(1)
    private double maxRefundRate = 0.05;

    @Min(0) @Max(1)
    private double maxLateShipmentRate = 0.10;

    @NotNull
    private String breachAction;

    @Min(7) @Max(90)
    private int evaluationWindowDays = 30;
}
