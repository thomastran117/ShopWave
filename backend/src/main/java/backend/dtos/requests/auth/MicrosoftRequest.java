package backend.dtos.requests.auth;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MicrosoftRequest {
    @NotEmpty(message = "Token cannot be empty")
    private String token;  
}
