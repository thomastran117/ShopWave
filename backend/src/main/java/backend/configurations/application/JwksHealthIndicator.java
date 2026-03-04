package backend.configurations.application;

import backend.configurations.environment.EnvironmentSetting;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Health indicator for Microsoft OAuth JWKS endpoint reachability.
 * Replaces startup reachability checks so app startup is not blocked by external services;
 * health endpoint reports JWKS status instead.
 */
@Component
public class JwksHealthIndicator implements HealthIndicator {

    private static final String JWKS_KEY = "jwks";

    private final EnvironmentSetting env;

    public JwksHealthIndicator(EnvironmentSetting env) {
        this.env = env;
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
        RestTemplate rest = jwksRestTemplate();
        try {
            rest.getForObject(jwksUri, String.class);
            return Health.up().withDetail(JWKS_KEY, "reachable").build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail(JWKS_KEY, "unreachable")
                    .withDetail("message", e.getMessage())
                    .build();
        }
    }

    private RestTemplate jwksRestTemplate() {
        var jwks = env.getSecurity().getJwks();
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(jwks.getConnectTimeoutMs()));
        factory.setReadTimeout(Duration.ofMillis(jwks.getReadTimeoutMs()));
        return new RestTemplate(factory);
    }
}
