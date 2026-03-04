package backend.services.impl;

import backend.aspects.OAuthResilient;
import backend.configurations.environment.EnvironmentSetting;
import backend.http.OAuthGoogleHttpTransportFactory;
import backend.models.other.OAuthUser;
import backend.security.oauth.InvalidOAuthTokenException;
import backend.security.oauth.OAuthClaimUtils;
import backend.security.oauth.OAuthNotSupportedException;
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
    private final GoogleIdTokenVerifier googleVerifier;
    private final JwtDecoder microsoftDecoder;
    private final int maxTokenLength;

    public OAuthServiceImpl(
            EnvironmentSetting env,
            @Qualifier("microsoftJwtDecoder") JwtDecoder microsoftDecoder) {
        this.googleClientId = env.getSecurity().getGoogleClientId();
        this.googleVerifier = buildGoogleVerifier(this.googleClientId, env.getSecurity().getOauthGoogle());
        this.microsoftDecoder = microsoftDecoder;
        int configured = env.getSecurity().getOauthMaxTokenLength();
        this.maxTokenLength = configured > 0 ? configured : DEFAULT_MAX_TOKEN_LENGTH;
    }

    private static GoogleIdTokenVerifier buildGoogleVerifier(String clientId, EnvironmentSetting.Security.OAuthGoogle timeouts) {
        if (clientId == null || clientId.isBlank()) {
            throw new IllegalStateException(
                    "Google OAuth client ID is not configured (set app.security.google-client-id)");
        }
        int connectMs = timeouts != null ? timeouts.getConnectTimeoutMs() : 5_000;
        int readMs = timeouts != null ? timeouts.getReadTimeoutMs() : 10_000;
        HttpTransport transport = OAuthGoogleHttpTransportFactory.build(connectMs, readMs);
        return new GoogleIdTokenVerifier.Builder(transport, GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(clientId))
                .setIssuers(List.of("https://accounts.google.com", "accounts.google.com"))
                .build();
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
    public OAuthUser verifyAppleToken(String appleToken) {
        throw new OAuthNotSupportedException("Apple OAuth is not implemented");
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
            idToken = googleVerifier.verify(googleToken);
        } catch (GeneralSecurityException e) {
            throw new InvalidOAuthTokenException("Invalid Google ID token", e);
        } catch (Exception e) {
            // Only treat as transient when failure is clearly upstream (IO, 5xx, timeouts); cert fetch etc. must not be mis-categorized as invalid token
            if (OAuthRetryable.isRetryable(e)) {
                throw new OAuthProviderTransientException("Google token verification failed", e);
            }
            // Unknown/non-transient: wrap as verification error so CB can record it; do not treat as invalid token
            throw new OAuthVerificationError("Google token verification failed", e);
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
