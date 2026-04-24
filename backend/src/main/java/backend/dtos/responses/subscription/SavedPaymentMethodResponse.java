package backend.dtos.responses.subscription;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SavedPaymentMethodResponse {
    private Long id;
    private String stripePaymentMethodId;
    private String brand;
    private String last4;
    private Integer expMonth;
    private Integer expYear;
    private boolean isDefault;
}
