package backend.configurations.application;

import backend.configurations.environment.EnvironmentSetting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * Logs OAuth configuration warnings early at startup. Missing provider credentials are
 * non-fatal: the corresponding OAuth bean is simply not registered and requests to that
 * provider will receive a 503. This validator only logs; it never prevents startup.
 */
@Component("oauthConfigValidator")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class OAuthConfigValidator {

    private static final Logger log = LoggerFactory.getLogger(OAuthConfigValidator.class);

    private final EnvironmentSetting env;

    public OAuthConfigValidator(EnvironmentSetting env) {
        this.env = env;
    }

    @PostConstruct
    void validateOAuthConfig() {
        if (env == null || env.getSecurity() == null) {
            log.warn("OAuth config validation skipped: app.security configuration is not available.");
            return;
        }

        EnvironmentSetting.Security security = env.getSecurity();

        if (!hasText(security.getGoogleClientId())) {
            log.warn("Google OAuth is not configured (GOOGLE_CLIENT_ID not set). POST /auth/google will be unavailable.");
        }
        if (!hasText(security.getMicrosoftClientId())) {
            log.warn("Microsoft OAuth is not configured (MICROSOFT_CLIENT_ID not set). POST /auth/microsoft will be unavailable.");
        }
        if (!hasText(security.getAppleClientId())) {
            log.warn("Apple OAuth is not configured (APPLE_CLIENT_ID not set). POST /auth/apple will be unavailable.");
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
