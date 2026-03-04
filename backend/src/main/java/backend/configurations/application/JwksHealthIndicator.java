package backend.configurations.application;

import backend.configurations.environment.EnvironmentSetting;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Health indicator for Microsoft OAuth JWKS endpoint reachability.
 * Uses shared {@code oauthJwksRestTemplate} so timeout and client config stay in sync with JWKS decoder.
 */
@Component
public class JwksHealthIndicator implements HealthIndicator {

    private static final String JWKS_KEY = "jwks";

    private final EnvironmentSetting env;
    private final RestTemplate oauthJwksRestTemplate;

    public JwksHealthIndicator(EnvironmentSetting env,
                                @Qualifier("oauthJwksRestTemplate") RestTemplate oauthJwksRestTemplate) {
        this.env = env;
        this.oauthJwksRestTemplate = oauthJwksRestTemplate;
    }

    @Override
    public Health health() {
        String jwksUri = env.getSecurity().getMicrosoftJwksUri();
        if (jwksUri == null || jwksUri.isBlank()) {
            return Health.unknown().withDetail(JWKS_KEY, "JWKS URI not configured").build();
        }
        if (!jwksUri.startsWith("https://")) {
            return Health.down().withDetail(JWKS_KEY, "JWKS URI must use HTTPS").build();
        }
        try {
            oauthJwksRestTemplate.getForObject(jwksUri, String.class);
            return Health.up().withDetail(JWKS_KEY, "reachable").build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail(JWKS_KEY, "unreachable")
                    .withDetail("message", e.getMessage())
                    .build();
        }
    }
}
