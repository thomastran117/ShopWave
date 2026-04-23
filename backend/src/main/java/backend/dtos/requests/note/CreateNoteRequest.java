package backend.dtos.requests.note;

import backend.annotations.safeText.SafeText;
import backend.models.enums.NoteEntityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateNoteRequest(
        @NotNull NoteEntityType entityType,
        @NotNull Long entityId,
        @NotBlank @SafeText @Size(max = 2000) String body
) {}
