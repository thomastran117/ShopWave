package backend.dtos.requests.product;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SetProductAttributesRequest {

    @NotNull
    @Valid
    private List<AttributeItem> attributes;

    @Getter
    @Setter
    public static class AttributeItem {

        @NotBlank(message = "Attribute name is required")
        private String name;

        @NotBlank(message = "Attribute value is required")
        private String value;

        private int displayOrder = 0;
    }
}
