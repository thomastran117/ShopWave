package backend.dtos.requests.segment;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCustomerSegmentRequest {

    @Size(max = 100)
    private String name;

    @Size(max = 500)
    private String description;
}
