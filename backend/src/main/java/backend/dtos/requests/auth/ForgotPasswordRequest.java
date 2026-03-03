package backend.dtos.requests.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ForgotPasswordRequest {
    @Email(message = "Invalid email format")
    @NotEmpty(message = "Email cannot be empty")
    private String email; 
}
