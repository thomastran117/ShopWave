package backend.services.impl;

import backend.aspects.OAuthResilient;
import backend.configurations.environment.EnvironmentSetting;
import backend.models.other.OAuthUser;
import backend.security.oauth.InvalidOAuthTokenException;
import backend.security.oauth.OAuthClaimUtils;
import backend.security.oauth.OAuthNotSupportedException;
import backend.security.oauth.OAuthProviderTransientException;
import backend.security.oauth.OAuthRetryable;
import backend.security.oauth.MicrosoftIssuerValidator;
import backend.services.intf.OAuthService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Verifies OAuth ID tokens from Google and Microsoft. The client sends the token
 * obtained from the provider; this service verifies it (signature, audience, issuer)
 * and returns user claims. Retry and circuit breaker are applied by {@link backend.aspects.OAuthRetryAspect}.
 */
@Service
public class OAuthServiceImpl implements OAuthService {

    private static final Logger log = LoggerFactory.getLogger(OAuthServiceImpl.class);

    /** Access token type; ID tokens use "JWT". Reject access tokens. */
    private static final String HEADER_TYP_ACCESS_TOKEN = "at+jwt";
    private static final String HEADER_ALG_NONE = "none";

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
        return new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
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
            if (!MicrosoftIssuerValidator.isValid(iss)) {
                return OAuth2TokenValidatorResult.failure(new OAuth2Error(OAuth2ErrorCodes.INVALID_REQUEST, "Invalid issuer", null));
            }
            Map<String, Object> headers = jwt.getHeaders();
            Object typ = headers != null ? headers.get("typ") : null;
            if (typ != null && HEADER_TYP_ACCESS_TOKEN.equalsIgnoreCase(typ.toString())) {
                return OAuth2TokenValidatorResult.failure(new OAuth2Error(OAuth2ErrorCodes.INVALID_REQUEST, "Invalid token type", null));
            }
            Object alg = headers != null ? headers.get("alg") : null;
            if (alg != null && HEADER_ALG_NONE.equalsIgnoreCase(alg.toString())) {
                return OAuth2TokenValidatorResult.failure(new OAuth2Error(OAuth2ErrorCodes.INVALID_REQUEST, "Invalid algorithm", null));
            }
            Object kid = headers != null ? headers.get("kid") : null;
            if (kid == null || kid.toString().isBlank()) {
                return OAuth2TokenValidatorResult.failure(new OAuth2Error(OAuth2ErrorCodes.INVALID_REQUEST, "Missing kid header", null));
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
        throw new OAuthNotSupportedException("Apple OAuth is not implemented");
    }

    @Override
    @OAuthResilient
    public OAuthUser verifyGoogleToken(String googleToken) {
        if (googleToken == null || googleToken.isBlank()) {
            throw new InvalidOAuthTokenException("Invalid Google ID token");
        }
        if (log.isDebugEnabled()) {
            log.debug("Verifying Google ID token");
        }
        GoogleIdToken idToken;
        try {
            idToken = googleVerifier.verify(googleToken);
        } catch (GeneralSecurityException e) {
            throw new InvalidOAuthTokenException("Invalid Google ID token", e);
        } catch (IOException e) {
            throw new OAuthProviderTransientException("Google token verification failed", e);
        } catch (RuntimeException e) {
            if (OAuthRetryable.isRetryable(e)) {
                throw new OAuthProviderTransientException("Google token verification failed", e);
            }
            throw new InvalidOAuthTokenException("Invalid Google ID token", e);
        }
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
        if (log.isDebugEnabled()) {
            log.debug("Verifying Microsoft ID token");
        }
        Jwt jwt;
        try {
            jwt = microsoftDecoder.decode(microsoftToken);
        } catch (JwtException e) {
            throw new InvalidOAuthTokenException("Invalid Microsoft token", e);
        } catch (IOException e) {
            throw new OAuthProviderTransientException("Microsoft token verification failed", e);
        } catch (ResourceAccessException e) {
            throw new OAuthProviderTransientException("Microsoft token verification failed", e);
        } catch (HttpServerErrorException e) {
            throw new OAuthProviderTransientException("Microsoft token verification failed", e);
        }
        String email = OAuthClaimUtils.getClaim(jwt, "preferred_username", "email");
        if (email == null || email.isBlank()) {
            throw new InvalidOAuthTokenException("Missing Microsoft email claim");
        }
        String name = OAuthClaimUtils.getClaim(jwt, "name", null);
        if (name == null || name.isBlank()) {
            name = email;
        }
        String sub = OAuthClaimUtils.getClaim(jwt, "sub", null);
        if (sub == null || sub.isBlank()) {
            throw new InvalidOAuthTokenException("Missing Microsoft sub claim");
        }
        return new OAuthUser(sub, email, name, "microsoft");
    }
}
