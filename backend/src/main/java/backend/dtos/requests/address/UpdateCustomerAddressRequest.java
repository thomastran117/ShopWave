package backend.dtos.requests.address;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateCustomerAddressRequest {

    @Size(max = 50)
    private String label;

    @Size(max = 150)
    private String recipientName;

    @Size(max = 255)
    private String street;

    @Size(max = 255)
    private String street2;

    @Size(max = 100)
    private String city;

    @Size(max = 100)
    private String state;

    @Size(max = 20)
    private String postalCode;

    @Pattern(regexp = "^[A-Z]{2}$", message = "country must be a 2-letter ISO 3166-1 alpha-2 code")
    private String country;

    @Size(max = 30)
    private String phoneNumber;
}
