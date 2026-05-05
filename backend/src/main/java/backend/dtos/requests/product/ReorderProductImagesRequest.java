package backend.dtos.requests.product;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ReorderProductImagesRequest {

    @NotEmpty(message = "Image IDs list must not be empty")
    @Size(max = 5, message = "Cannot reorder more than 5 images")
    private List<Long> imageIds;
}
