package backend.controllers;

import java.util.Map;
import java.util.Collections;

// Import java classes
import backend.interfaces.UserService;
import backend.services.AuthServiceImpl;
import io.jsonwebtoken.Claims;
import backend.models.User;
import backend.dtos.MessageResponseDto;
import backend.dtos.LoginRequestDto;
import backend.dtos.SignupRequestDto;
import backend.dtos.UserResponseDto;
import backend.exceptions.AuthenticationException;
import backend.dtos.PasswordChangeRequestDto;
import backend.configs.EnvConfig;

// Spring imports
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Value;

//Google API
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;

// Other imports
import jakarta.validation.Valid;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// Controller listening for /auth routes
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final AuthServiceImpl authService;
    private final EnvConfig env;

    // Constructor to inject dependencies
    public AuthController(UserService userService, AuthServiceImpl authService, EnvConfig env) {
        this.userService = userService;
        this.authService = authService;
        this.env = env;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDto request, HttpServletResponse response) {
        try {
            User user = userService.login(request.getEmail(), request.getPassword());
            Map<String, Object> tokens = authService.generateTokenPair(user.getId().intValue(), user.getUsertype());
            String refreshToken = (String) tokens.remove("refreshToken");
            String accessToken = (String) tokens.remove("accessToken");

            ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(7 * 24 * 60 * 60)
                .build();

            response.addHeader("Set-Cookie", cookie.toString());

            return ResponseEntity.ok(
                    new UserResponseDto(accessToken, user.getEmail(), user.getUsertype(), user.getId())
                );
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponseDto(e.getMessage()));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        if (refreshToken == null || !authService.validateRefreshToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid or expired refresh token"));
        }

        int userId = Integer.parseInt(authService.getClaimFromToken(refreshToken, Claims::getSubject));
        String role = authService.getClaimFromToken(refreshToken, claims -> claims.get("role", String.class));

        String newAccessToken = authService.generateAccessToken(userId, role);
        String newRefreshToken = authService.rotateRefreshToken(refreshToken);

        ResponseCookie cookie = ResponseCookie.from("refreshToken", newRefreshToken)
            .httpOnly(true)
            .secure(false)
            .sameSite("Lax")
            .path("/")
            .maxAge(7 * 24 * 60 * 60)
            .build();

        response.addHeader("Set-Cookie", cookie.toString());

        return ResponseEntity.ok(Map.of(
                "accessToken", newAccessToken,
                "tokenType", "Bearer",
                "expiresIn", env.getJwtValidity()
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable long id) {
        try {
            userService.delete(id);
            return ResponseEntity.ok(new MessageResponseDto("User deleted successfully."));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponseDto(e.getMessage()));
        }
    }

    @PutMapping("/change-password/{id}")
    public ResponseEntity<?> changePassword(@PathVariable long id, @RequestBody PasswordChangeRequestDto request) {
        try {
            userService.changePassword(id, request.getNewPassword());
            return ResponseEntity.ok(new MessageResponseDto("Password changed successfully."));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponseDto(e.getMessage()));
        }
    }

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody Map<String, String> body, HttpServletResponse response) throws Exception {
        String idTokenString = body.get("idToken");
        if (idTokenString == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing idToken"));
        }

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(), JacksonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(env.getGoogleClientId()))
                .build();

        GoogleIdToken idToken = verifier.verify(idTokenString);
        if (idToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid ID token"));
        }

        Payload payload = idToken.getPayload();
        String email = payload.getEmail();
        boolean emailVerified = Boolean.TRUE.equals(payload.getEmailVerified());
        if (!emailVerified) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Email not verified"));
        }

        User user = userService.loginOrSignupGoogle(email);
        Map<String, Object> tokens = authService.generateTokenPair(user.getId().intValue(), user.getUsertype());

        String refreshToken = (String) tokens.remove("refreshToken");
        String accessToken = (String) tokens.remove("accessToken");

        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
            .httpOnly(true)
            .secure(false)
            .sameSite("Lax")
            .path("/")
            .maxAge(7 * 24 * 60 * 60)
            .build();

        response.addHeader("Set-Cookie", cookie.toString());

        return ResponseEntity.ok(
                new UserResponseDto(accessToken, user.getEmail(), user.getUsertype(), user.getId())
            );
    }

    @GetMapping("/hello")
    public String Hello() {
        return "hello authorized user!";
    }
}
