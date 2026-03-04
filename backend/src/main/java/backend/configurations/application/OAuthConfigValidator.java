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
        var security = env.getSecurity();
        String googleId = security.getGoogleClientId();
        String microsoftId = security.getMicrosoftClientId();
        String jwksUri = security.getMicrosoftJwksUri();

        if (googleId == null || googleId.isBlank()) {
            throw new IllegalStateException(
                    "Google OAuth client ID is not configured. Set app.security.google-client-id.");
        }
        if (microsoftId == null || microsoftId.isBlank()) {
            throw new IllegalStateException(
                    "Microsoft OAuth client ID is not configured. Set app.security.microsoft-client-id.");
        }
        if (jwksUri == null || jwksUri.isBlank()) {
            throw new IllegalStateException(
                    "Microsoft OAuth JWKS URI is not configured. Set app.security.microsoft-jwks-uri.");
        }
        log.debug("OAuth configuration validated (Google and Microsoft client IDs, JWKS URI present).");
    }
}
