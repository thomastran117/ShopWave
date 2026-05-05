package backend.dtos.responses.note;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class InternalNoteResponse {
    private Long id;
    private String entityType;
    private Long entityId;
    private Long authorId;
    private String authorName;
    private String body;
    private Instant createdAt;
}
