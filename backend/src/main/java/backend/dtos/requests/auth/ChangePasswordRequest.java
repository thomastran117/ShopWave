package backend.dtos.requests.auth;

import backend.annotations.strongPassword.StrongPassword;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ChangePasswordRequest {
    @NotEmpty(message = "Token cannot be empty")
    private String token;  

    @StrongPassword(minLength = 8)
    private String password;
}
