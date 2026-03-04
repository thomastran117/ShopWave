package backend.services.impl;

import backend.configurations.environment.EnvironmentSetting;
import backend.exceptions.http.NotImplementedException;
import backend.exceptions.http.UnauthorizedException;
import backend.models.other.OAuthUser;
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
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

/**
 * Verifies OAuth ID tokens from Google and Microsoft. The client sends the token
 * obtained from the provider; this service verifies it (signature, audience, issuer)
 * and returns user claims. Retry and circuit breaker are applied by {@link backend.aspects.OAuthRetryAspect}.
 */
@Service
public class OAuthServiceImpl implements OAuthService {

    private static final String MICROSOFT_KEYS_URI = "https://login.microsoftonline.com/common/discovery/v2.0/keys";

    private final String googleClientId;
    private final String microsoftClientId;
    private final GoogleIdTokenVerifier googleVerifier;
    private final JwtDecoder microsoftDecoder;

    public OAuthServiceImpl(EnvironmentSetting env) {
        this.googleClientId = env.getSecurity().getGoogleClientId();
        this.microsoftClientId = env.getSecurity().getMicrosoftClientId();
        this.googleVerifier = buildGoogleVerifier(googleClientId);
        this.microsoftDecoder = buildMicrosoftDecoder(microsoftClientId);
    }

    private static GoogleIdTokenVerifier buildGoogleVerifier(String clientId) {
        if (clientId == null || clientId.isBlank()) {
            throw new IllegalStateException("Google OAuth client ID is not configured (set app.security.google-client-id)");
        }
        return new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(clientId))
                .build();
    }

    private static JwtDecoder buildMicrosoftDecoder(String clientId) {
        if (clientId == null || clientId.isBlank()) {
            throw new IllegalStateException("Microsoft OAuth client ID is not configured (set app.security.microsoft-client-id)");
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
            if (!iss.startsWith("https://login.microsoftonline.com/") || !iss.endsWith("/v2.0")) {
                return OAuth2TokenValidatorResult.failure(new OAuth2Error(OAuth2ErrorCodes.INVALID_REQUEST, "Invalid issuer: " + iss, null));
            }
            return OAuth2TokenValidatorResult.success();
        };
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(MICROSOFT_KEYS_URI)
                .build();
        decoder.setJwtValidator(customValidator);
        return decoder;
    }

    @Override
    public OAuthUser verifyAppleToken(String appleToken) {
        throw new NotImplementedException("Apple OAuth is not implemented");
    }

    @Override
    public OAuthUser verifyGoogleToken(String googleToken) throws IOException {
        GoogleIdToken idToken;
        try {
            idToken = googleVerifier.verify(googleToken);
        } catch (GeneralSecurityException e) {
            throw new UnauthorizedException("Invalid Google ID token");
        } catch (RuntimeException e) {
            throw new UnauthorizedException("Invalid Google ID token");
        }
        // IOException propagates so OAuthRetryAspect can retry
        if (idToken == null) {
            throw new UnauthorizedException("Invalid Google ID token");
        }
        GoogleIdToken.Payload payload = idToken.getPayload();
        String sub = payload.getSubject();
        String email = payload.getEmail();
        String name = payload.get("name") != null ? (String) payload.get("name") : (email != null ? email : "");
        if (email == null || email.isBlank()) {
            throw new UnauthorizedException("Missing Google email claim");
        }
        return new OAuthUser(sub != null ? sub : "", email, name != null ? name : email, "google");
    }

    @Override
    public OAuthUser verifyMicrosoftToken(String microsoftToken) {
        Jwt jwt;
        try {
            jwt = microsoftDecoder.decode(microsoftToken);
        } catch (Exception e) {
            throw new UnauthorizedException("Invalid Microsoft token");
        }
        String email = getClaim(jwt, "preferred_username", "email");
        if (email == null || email.isBlank()) {
            throw new UnauthorizedException("Missing Microsoft email claim");
        }
        String name = getClaim(jwt, "name", null);
        if (name == null || name.isBlank()) {
            name = email;
        }
        String sub = getClaim(jwt, "sub", null);
        if (sub == null || sub.isBlank()) {
            throw new UnauthorizedException("Missing Microsoft sub claim");
        }
        return new OAuthUser(sub, email, name, "microsoft");
    }

    private static String getClaim(Jwt jwt, String preferred, String fallback) {
        Object val = jwt.getClaim(preferred);
        if (val != null && !val.toString().isBlank()) {
            return val.toString();
        }
        if (fallback != null) {
            Object fall = jwt.getClaim(fallback);
            if (fall != null && !fall.toString().isBlank()) {
                return fall.toString();
            }
        }
        return null;
    }
}
