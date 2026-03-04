package backend.services.impl;

import backend.aspects.OAuthResilient;
import backend.configurations.environment.EnvironmentSetting;
import backend.http.OAuthGoogleHttpTransportFactory;
import backend.models.other.OAuthUser;
import backend.security.oauth.GoogleDiscoveryIssuers;
import backend.security.oauth.InvalidOAuthTokenException;
import backend.security.oauth.OAuthClaimUtils;
import backend.security.oauth.OAuthProviderTransientException;
import backend.security.oauth.OAuthRetryable;
import backend.security.oauth.OAuthVerificationError;
import backend.services.intf.OAuthService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

/**
 * Verifies OAuth ID tokens from Google and Microsoft. The client sends the token
 * obtained from the provider; this service verifies it (signature, audience, issuer)
 * and returns user claims. Retry and circuit breaker are applied by {@link backend.aspects.OAuthRetryAspect}.
 * Logging: debug must not include tokens, PII, or provider responses to avoid sensitive data leakage.
 */
@Service
public class OAuthServiceImpl implements OAuthService {

    private static final Logger log = LoggerFactory.getLogger(OAuthServiceImpl.class);

    /**
     * Maximum token length to avoid oversized token attacks. Typical Google/Microsoft ID tokens
     * are under 4KB; 16KB allows headroom. Configurable via app.security.oauth-max-token-length.
     */
    private static final int DEFAULT_MAX_TOKEN_LENGTH = 16_384;

    private final String googleClientId;
    private final int googleConnectMs;
    private final int googleReadMs;
    private volatile GoogleIdTokenVerifier googleVerifier;
    private final JwtDecoder microsoftDecoder;
    private final int maxTokenLength;

    public OAuthServiceImpl(
            EnvironmentSetting env,
            @Qualifier("microsoftJwtDecoder") JwtDecoder microsoftDecoder) {
        this.googleClientId = env.getSecurity().getGoogleClientId();
        EnvironmentSetting.Security.OAuthGoogle timeouts = env.getSecurity().getOauthGoogle();
        this.googleConnectMs = timeouts != null ? timeouts.getConnectTimeoutMs() : 5_000;
        this.googleReadMs = timeouts != null ? timeouts.getReadTimeoutMs() : 10_000;
        this.microsoftDecoder = microsoftDecoder;
        int configured = env.getSecurity().getOauthMaxTokenLength();
        this.maxTokenLength = configured > 0 ? configured : DEFAULT_MAX_TOKEN_LENGTH;
    }

    /** Lazy-init verifier so discovery fetch does not block startup. Full construction inside lock to avoid racy partial visibility. */
    private GoogleIdTokenVerifier getGoogleVerifier() {
        if (googleVerifier == null) {
            synchronized (this) {
                if (googleVerifier == null) {
                    GoogleIdTokenVerifier built = buildGoogleVerifier(googleClientId, googleConnectMs, googleReadMs);
                    googleVerifier = built;
                }
            }
        }
        return googleVerifier;
    }

    private static GoogleIdTokenVerifier buildGoogleVerifier(String clientId, int connectMs, int readMs) {
        if (clientId == null || clientId.isBlank()) {
            throw new IllegalStateException(
                    "Google OAuth client ID is not configured (set app.security.google-client-id)");
        }
        HttpTransport transport = OAuthGoogleHttpTransportFactory.build(connectMs, readMs);
        List<String> issuers = GoogleDiscoveryIssuers.getIssuers(transport);
        return new GoogleIdTokenVerifier.Builder(transport, GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(clientId))
                .setIssuers(issuers)
                .build();
    }

    /**
     * True if the throwable (or its cause chain) indicates invalid token / validation failure
     * (e.g. malformed JWT, bad format). Such failures should be InvalidOAuthTokenException so
     * they do not open the circuit breaker.
     */
    private static boolean isValidationFailure(Throwable t) {
        Throwable current = t;
        while (current != null) {
            if (current instanceof IllegalArgumentException
                    || current instanceof NumberFormatException
                    || current instanceof IllegalStateException) {
                return true;
            }
            String name = current.getClass().getName();
            if (name.contains("JsonParse") || name.contains("JsonProcessing")
                    || name.contains("JwtException") || name.contains("SignatureException")) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    /** Safely extracts a string claim from Google payload; avoids ClassCastException when provider returns non-string. */
    private static String getStringClaimFromPayload(GoogleIdToken.Payload payload, String claimName) {
        Object value = payload.get(claimName);
        if (value == null) {
            return null;
        }
        if (value instanceof String s) {
            return s.isBlank() ? null : s;
        }
        if (value instanceof Number || value instanceof Boolean) {
            String s = value.toString();
            return (s == null || s.isBlank()) ? null : s;
        }
        return null;
    }

    /** Shared check for blank, invalid, or oversized tokens; throws before any verification. */
    private void requireValidTokenLength(String token, String providerLabel) {
        if (token == null || token.isBlank()) {
            throw new InvalidOAuthTokenException("Invalid " + providerLabel + " token");
        }
        int maxLen = maxTokenLength;
        if (token.length() > maxLen) {
            throw new InvalidOAuthTokenException("Invalid " + providerLabel + " token");
        }
    }

    @Override
    @OAuthResilient
    public OAuthUser verifyGoogleToken(String googleToken) {
        requireValidTokenLength(googleToken, "Google ID");
        if (log.isDebugEnabled()) {
            log.debug("Verifying Google ID token");
        }
        GoogleIdToken idToken;
        try {
            idToken = getGoogleVerifier().verify(googleToken);
        } catch (GeneralSecurityException e) {
            throw new InvalidOAuthTokenException("Invalid Google ID token", e);
        } catch (Exception e) {
            if (OAuthRetryable.isRetryable(e)) {
                throw new OAuthProviderTransientException("Google token verification failed", e);
            }
            // Validation failures (malformed JWT, bad format) should not open the circuit
            if (isValidationFailure(e)) {
                throw new InvalidOAuthTokenException("Invalid Google ID token", e);
            }
            throw new OAuthVerificationError("Google token verification failed", e);
        }
        if (idToken == null) {
            throw new InvalidOAuthTokenException("Invalid Google ID token");
        }
        GoogleIdToken.Payload payload = idToken.getPayload();
        String sub = payload.getSubject();
        String email = payload.getEmail();
        String name = getStringClaimFromPayload(payload, "name");
        if (name == null || name.isBlank()) {
            name = email != null ? email : "";
        }
        if (email == null || email.isBlank()) {
            throw new InvalidOAuthTokenException("Missing Google email claim");
        }
        return new OAuthUser(sub != null ? sub : "", email, (name != null && !name.isBlank()) ? name : email, "google");
    }

    @Override
    @OAuthResilient
    public OAuthUser verifyMicrosoftToken(String microsoftToken) {
        requireValidTokenLength(microsoftToken, "Microsoft");
        if (log.isDebugEnabled()) {
            log.debug("Verifying Microsoft ID token");
        }
        Jwt jwt;
        try {
            jwt = microsoftDecoder.decode(microsoftToken);
        } catch (JwtException e) {
            throw new InvalidOAuthTokenException("Invalid Microsoft token", e);
        } catch (Exception e) {
            if (OAuthRetryable.isRetryable(e)) {
                throw new OAuthProviderTransientException("Microsoft token verification failed", e);
            }
            throw new OAuthVerificationError("Microsoft token verification failed", e);
        }
        if (jwt == null) {
            throw new InvalidOAuthTokenException("Invalid Microsoft token (no claims)");
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
