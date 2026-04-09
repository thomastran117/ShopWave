package backend.dtos.requests.inventory;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateLocationRequest {

    @Size(max = 255, message = "Name must be at most 255 characters")
    private String name;

    @Size(max = 100, message = "Code must be at most 100 characters")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$",
             message = "Code may only contain letters, digits, hyphens, and underscores")
    private String code;

    @Size(max = 500, message = "Address must be at most 500 characters")
    private String address;

    @Size(max = 100, message = "City must be at most 100 characters")
    private String city;

    @Size(max = 100, message = "Country must be at most 100 characters")
    private String country;

    private Boolean active;

    private Integer displayOrder;
}
