package backend.configurations.application;

import backend.configurations.environment.EnvironmentSetting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * Validates required OAuth environment configuration keys early at startup,
 * so failures are explicit and not deferred to bean creation. Run before
 * OAuth beans so a single place owns validation.
 * Uses IllegalStateException for fail-fast; could be replaced with
 * {@code @Validated} on a dedicated OAuthConfigProperties bean if preferred.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class OAuthConfigValidator {

    private static final Logger log = LoggerFactory.getLogger(OAuthConfigValidator.class);

    private final EnvironmentSetting env;

    public OAuthConfigValidator(EnvironmentSetting env) {
        this.env = env;
    }

    @PostConstruct
    void validateOAuthConfig() {
        if (env == null) {
            return;
        }
        var oauth = env.getOauth();
        boolean failFast = (oauth != null && oauth.isValidateConfigAtStartup());
        var security = env.getSecurity();
        if (security == null) {
            if (failFast) {
                throw new IllegalStateException("OAuth configuration missing: app.security is not available.");
            }
            log.warn("OAuth config validation skipped: security config not available.");
            return;
        }
        String googleId = security.getGoogleClientId();
        String microsoftId = security.getMicrosoftClientId();
        String jwksUri = security.getMicrosoftJwksUri();

        if (googleId == null || googleId.isBlank()) {
            if (failFast) {
                throw new IllegalStateException(
                        "Google OAuth client ID is not configured. Set app.security.google-client-id.");
            }
            log.warn("OAuth config: Google client ID not set (app.security.google-client-id). Set app.oauth.validate-config-at-startup=true to fail startup.");
        }
        if (microsoftId == null || microsoftId.isBlank()) {
            if (failFast) {
                throw new IllegalStateException(
                        "Microsoft OAuth client ID is not configured. Set app.security.microsoft-client-id.");
            }
            log.warn("OAuth config: Microsoft client ID not set. Set app.oauth.validate-config-at-startup=true to fail startup.");
        }
        if (jwksUri == null || jwksUri.isBlank()) {
            if (failFast) {
                throw new IllegalStateException(
                        "Microsoft OAuth JWKS URI is not configured. Set app.security.microsoft-jwks-uri.");
            }
            log.warn("OAuth config: Microsoft JWKS URI not set. Set app.oauth.validate-config-at-startup=true to fail startup.");
        } else {
            String trimmed = jwksUri.trim().toLowerCase();
            if (!trimmed.startsWith("https://")) {
                if (failFast) {
                    throw new IllegalStateException(
                            "Microsoft OAuth JWKS URI must use HTTPS. Got: " + jwksUri);
                }
                log.warn("OAuth config: Microsoft JWKS URI must use HTTPS. Set app.oauth.validate-config-at-startup=true to fail startup.");
            }
        }
        if (!failFast && (googleId == null || googleId.isBlank() || microsoftId == null || microsoftId.isBlank()
                || jwksUri == null || jwksUri.isBlank() || (jwksUri != null && !jwksUri.trim().toLowerCase().startsWith("https://")))) {
            log.warn("OAuth config validation completed with warnings; startup continued. Set app.oauth.validate-config-at-startup=true for strict validation.");
        } else {
            log.debug("OAuth configuration validated (Google and Microsoft client IDs, JWKS URI present and HTTPS).");
        }
    }
}
