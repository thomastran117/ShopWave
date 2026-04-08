package backend.dtos.requests.product;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProductOptionRequest {

    @Size(max = 100, message = "Option name must be at most 100 characters")
    private String name;
}
