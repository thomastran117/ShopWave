package backend.dtos.responses.support;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class TicketMessageResponse {
    private Long id;
    private Long authorId;
    private String authorName;
    private String authorRole;
    private String body;
    private Instant createdAt;
}
