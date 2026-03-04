package backend.services.impl;

import backend.aspects.OAuthResilient;
import backend.configurations.environment.EnvironmentSetting;
import backend.exceptions.http.NotImplementedException;
import backend.models.other.OAuthUser;
import backend.security.oauth.InvalidOAuthTokenException;
import backend.security.oauth.OAuthProviderTransientException;
import backend.services.intf.OAuthService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Verifies OAuth ID tokens from Google and Microsoft. The client sends the token
 * obtained from the provider; this service verifies it (signature, audience, issuer)
 * and returns user claims. Retry and circuit breaker are applied by {@link backend.aspects.OAuthRetryAspect}.
 */
@Service
public class OAuthServiceImpl implements OAuthService {

    /** Accepts tenant-specific, common, organizations, and consumers issuers; optional trailing slash. */
    private static final Pattern MICROSOFT_ISSUER_PATTERN = Pattern.compile(
            "^https://login\\.microsoftonline\\.com/[^/]+/v2\\.0/?$");

    private final String googleClientId;
    private final String microsoftClientId;
    private final GoogleIdTokenVerifier googleVerifier;
    private final JwtDecoder microsoftDecoder;

    public OAuthServiceImpl(EnvironmentSetting env) {
        String googleId = env.getSecurity().getGoogleClientId();
        String microsoftId = env.getSecurity().getMicrosoftClientId();
        if (googleId == null || googleId.isBlank()) {
            throw new IllegalStateException("Google OAuth client ID is not configured (set app.security.google-client-id)");
        }
        if (microsoftId == null || microsoftId.isBlank()) {
            throw new IllegalStateException("Microsoft OAuth client ID is not configured (set app.security.microsoft-client-id)");
        }
        this.googleClientId = googleId;
        this.microsoftClientId = microsoftId;
        String microsoftJwksUri = env.getSecurity().getMicrosoftJwksUri();
        if (microsoftJwksUri == null || microsoftJwksUri.isBlank()) {
            throw new IllegalStateException("Microsoft OAuth JWKS URI is not configured (set app.security.microsoft-jwks-uri)");
        }
        this.googleVerifier = buildGoogleVerifier(googleClientId);
        this.microsoftDecoder = buildMicrosoftDecoder(microsoftClientId, microsoftJwksUri);
    }

    private static GoogleIdTokenVerifier buildGoogleVerifier(String clientId) {
        if (clientId == null || clientId.isBlank()) {
            throw new IllegalStateException("Google OAuth client ID is not configured (set app.security.google-client-id)");
        }
        return new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(clientId))
                .build();
    }

    private static JwtDecoder buildMicrosoftDecoder(String clientId, String jwksUri) {
        if (clientId == null || clientId.isBlank()) {
            throw new IllegalStateException("Microsoft OAuth client ID is not configured (set app.security.microsoft-client-id)");
        }
        if (jwksUri == null || jwksUri.isBlank()) {
            throw new IllegalStateException("Microsoft OAuth JWKS URI is not configured (set app.security.microsoft-jwks-uri)");
        }
        String audience = clientId;
        OAuth2TokenValidator<Jwt> defaultValidator = JwtValidators.createDefault();
        OAuth2TokenValidator<Jwt> customValidator = jwt -> {
            OAuth2TokenValidatorResult defaultResult = defaultValidator.validate(jwt);
            if (defaultResult.hasErrors()) {
                return defaultResult;
            }
            List<String> audiences = jwt.getAudience() != null ? jwt.getAudience() : List.of();
            if (!audiences.contains(audience)) {
                return OAuth2TokenValidatorResult.failure(new OAuth2Error(OAuth2ErrorCodes.INVALID_REQUEST, "Invalid audience", null));
            }
            String iss = jwt.getIssuer() != null ? jwt.getIssuer().toString() : "";
            if (!MICROSOFT_ISSUER_PATTERN.matcher(iss).matches()) {
                return OAuth2TokenValidatorResult.failure(new OAuth2Error(OAuth2ErrorCodes.INVALID_REQUEST, "Invalid issuer", null));
            }
            return OAuth2TokenValidatorResult.success();
        };
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwksUri)
                .build();
        decoder.setJwtValidator(customValidator);
        return decoder;
    }

    @Override
    public OAuthUser verifyAppleToken(String appleToken) {
        throw new NotImplementedException("Apple OAuth is not implemented");
    }

    @Override
    @OAuthResilient
    public OAuthUser verifyGoogleToken(String googleToken) throws IOException {
        if (googleToken == null || googleToken.isBlank()) {
            throw new InvalidOAuthTokenException("Invalid Google ID token");
        }
        GoogleIdToken idToken;
        try {
            idToken = googleVerifier.verify(googleToken);
        } catch (GeneralSecurityException e) {
            throw new InvalidOAuthTokenException("Invalid Google ID token", e);
        } catch (RuntimeException e) {
            throw new OAuthProviderTransientException("Google token verification failed", e);
        }
        // IOException propagates so retry/CB can retry and record
        if (idToken == null) {
            throw new InvalidOAuthTokenException("Invalid Google ID token");
        }
        GoogleIdToken.Payload payload = idToken.getPayload();
        String sub = payload.getSubject();
        String email = payload.getEmail();
        String name = payload.get("name") != null ? (String) payload.get("name") : (email != null ? email : "");
        if (email == null || email.isBlank()) {
            throw new InvalidOAuthTokenException("Missing Google email claim");
        }
        return new OAuthUser(sub != null ? sub : "", email, name != null ? name : email, "google");
    }

    @Override
    @OAuthResilient
    public OAuthUser verifyMicrosoftToken(String microsoftToken) {
        if (microsoftToken == null || microsoftToken.isBlank()) {
            throw new InvalidOAuthTokenException("Invalid Microsoft token");
        }
        Jwt jwt;
        try {
            jwt = microsoftDecoder.decode(microsoftToken);
        } catch (JwtException e) {
            throw new InvalidOAuthTokenException("Invalid Microsoft token", e);
        } catch (Exception e) {
            throw new OAuthProviderTransientException("Microsoft token verification failed", e);
        }
        String email = getClaim(jwt, "preferred_username", "email");
        if (email == null || email.isBlank()) {
            throw new InvalidOAuthTokenException("Missing Microsoft email claim");
        }
        String name = getClaim(jwt, "name", null);
        if (name == null || name.isBlank()) {
            name = email;
        }
        String sub = getClaim(jwt, "sub", null);
        if (sub == null || sub.isBlank()) {
            throw new InvalidOAuthTokenException("Missing Microsoft sub claim");
        }
        return new OAuthUser(sub, email, name, "microsoft");
    }

    private static String getClaim(Jwt jwt, String preferred, String fallback) {
        return Optional.ofNullable(jwt.getClaim(preferred)).map(Object::toString).filter(s -> !s.isBlank())
                .or(() -> Optional.ofNullable(fallback).map(f -> jwt.getClaim(f)).map(Object::toString).filter(s -> !s.isBlank()))
                .orElse(null);
    }
}
