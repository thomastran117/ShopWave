package backend.dtos.requests.vendor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplyVendorRequest {

    @NotNull(message = "Vendor company ID is required")
    @Positive(message = "Vendor company ID must be positive")
    private Long vendorCompanyId;
}
