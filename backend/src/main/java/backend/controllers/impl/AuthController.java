package backend.controllers.impl;

import java.util.Map;

import backend.services.intf.AuthService;
import backend.services.intf.OAuthService;
import backend.services.intf.UserService;
import backend.dtos.responses.general.MessageResponse;
import backend.dtos.requests.auth.LoginRequest;
import backend.dtos.responses.auth.AuthResponse;
import backend.exceptions.http.AppHttpException;
import backend.exceptions.http.InternalServerErrorException;
import backend.dtos.requests.auth.ChangePasswordRequest;
import backend.annotations.requireAuth.RequireAuth;
import backend.models.other.OAuthUser;
import backend.security.oauth.InvalidOAuthTokenException;

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

import jakarta.validation.Valid;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final AuthService authService;
    private final OAuthService oauthService;

    public AuthController(UserService userService, AuthService authService, OAuthService oauthService) {
        this.userService = userService;
        this.authService = authService;
        this.oauthService = oauthService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        try {
            AuthService.LoginResult result = authService.login(request.getEmail(), request.getPassword());

            ResponseCookie cookie = ResponseCookie.from("refreshToken", result.refreshToken())
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(7 * 24 * 60 * 60)
                .build();

            response.addHeader("Set-Cookie", cookie.toString());

            return ResponseEntity.ok(
                    new AuthResponse(result.accessToken(), result.email(), result.usertype(), result.userId())
                );
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
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

        try {
            AuthService.RefreshResult result = authService.refresh(refreshToken);

            ResponseCookie cookie = ResponseCookie.from("refreshToken", result.refreshToken())
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(7 * 24 * 60 * 60)
                .build();

            response.addHeader("Set-Cookie", cookie.toString());

            return ResponseEntity.ok(Map.of(
                    "accessToken", result.accessToken(),
                    "tokenType", "Bearer",
                    "expiresIn", result.expiresInSeconds()
            ));
        } catch (org.springframework.web.server.ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getReason()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }
        if (refreshToken != null) {
            authService.revokeRefreshToken(refreshToken);
        }
        ResponseCookie clearCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader("Set-Cookie", clearCookie.toString());
        return ResponseEntity.ok(new MessageResponse("Logged out."));
    }

    @RequireAuth
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable long id) {
        try {
            userService.delete(id);
            return ResponseEntity.ok(new MessageResponse("User deleted successfully."));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @RequireAuth
    @PutMapping("/change-password/{id}")
    public ResponseEntity<?> changePassword(@PathVariable long id, @RequestBody ChangePasswordRequest request) {
        try {
            userService.changePassword(id, request.getPassword());
            authService.revokeAllRefreshTokensForUser((int) id);
            return ResponseEntity.ok(new MessageResponse("Password changed successfully."));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody Map<String, String> body, HttpServletResponse response) {
        String idTokenString = body.get("idToken");
        if (idTokenString == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing idToken"));
        }

        try {
            OAuthUser oauthUser = oauthService.verifyGoogleToken(idTokenString);

            AuthService.LoginResult result = authService.loginOrSignupGoogle(oauthUser.email());

            ResponseCookie cookie = ResponseCookie.from("refreshToken", result.refreshToken())
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(7 * 24 * 60 * 60)
                .build();

            response.addHeader("Set-Cookie", cookie.toString());

            return ResponseEntity.ok(
                    new AuthResponse(result.accessToken(), result.email(), result.usertype(), result.userId())
                );
        } catch (InvalidOAuthTokenException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid or expired Google token"));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @RequireAuth
    @GetMapping("/hello")
    public String Hello() {
        return "hello authorized user!";
    }
}
