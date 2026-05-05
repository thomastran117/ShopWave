package backend.dtos.requests.vendor;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateVendorProfileRequest {

    @Size(max = 255, message = "Display name must not exceed 255 characters")
    private String displayName;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @Size(max = 255, message = "Support email must not exceed 255 characters")
    private String supportEmail;

    @Size(max = 30, message = "Support phone must not exceed 30 characters")
    private String supportPhone;

    @Size(max = 255, message = "Website must not exceed 255 characters")
    private String website;
}
