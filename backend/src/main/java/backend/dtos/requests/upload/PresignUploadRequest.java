package backend.dtos.requests.upload;

import backend.models.enums.UploadFolder;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PresignUploadRequest {

    @NotNull(message = "Folder is required")
    private UploadFolder folder;

    @NotBlank(message = "Content type is required")
    private String contentType;
}
