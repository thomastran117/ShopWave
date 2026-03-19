package backend.controllers.impl;

import java.util.Map;

import backend.services.intf.AuthService;
import backend.services.intf.OAuthService;
import backend.utilities.intf.Logger;
import backend.dtos.responses.general.MessageResponse;
import backend.dtos.requests.auth.LoginRequest;
import backend.dtos.responses.auth.AuthResponse;
import backend.exceptions.http.AppHttpException;
import backend.exceptions.http.InternalServerErrorException;
import backend.annotations.requireAuth.RequireAuth;
import backend.models.other.OAuthUser;
import backend.security.oauth.InvalidOAuthTokenException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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

    private final AuthService authService;
    private final OAuthService oauthService;
    private final Logger logger;
    
    public AuthController(AuthService authService, OAuthService oauthService, Logger logger) {
        this.authService = authService;
        this.oauthService = oauthService;
        this.logger = logger;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        try {
            logger.info("hello!");
            logger.debug("hello!");
            logger.critical("hello!");
            logger.warn("hello!");
            logger.error("hello!");

            AuthService.LoginResult result = authService.localAuthenicate(request.getEmail(), request.getPassword());

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

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody Map<String, String> body, HttpServletResponse response) {
        String idTokenString = body.get("idToken");
        if (idTokenString == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing idToken"));
        }

        try {
            OAuthUser oauthUser = oauthService.verifyGoogleToken(idTokenString);

            AuthService.LoginResult result = authService.googleAuthenicate(oauthUser.email());

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
