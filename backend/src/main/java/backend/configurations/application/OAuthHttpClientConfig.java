package backend.configurations.application;

import backend.configurations.environment.EnvironmentSetting;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Single place for OAuth-related HTTP client construction (timeouts, RestTemplate).
 * Used by JWKS (Microsoft) and keeps config in sync to avoid divergence and config drift.
 * Google OAuth uses a separate transport (Apache-based) built with the same timeout config.
 */
@Configuration
public class OAuthHttpClientConfig {

    /**
     * RestTemplate for JWKS and any OAuth HTTP calls that share the same timeout policy.
     * Connect/read timeouts come from app.security.jwks (same as Microsoft JWKS fetch).
     */
    @Bean("oauthJwksRestTemplate")
    @Qualifier("oauthJwksRestTemplate")
    public RestTemplate oauthJwksRestTemplate(EnvironmentSetting env) {
        EnvironmentSetting.Security.Jwks jwks = env.getSecurity().getJwks();
        return buildRestTemplateWithTimeouts(jwks.getConnectTimeoutMs(), jwks.getReadTimeoutMs());
    }

    /**
     * Builds a RestTemplate with the given connect and read timeouts (milliseconds).
     * Shared so Google and JWKS timeout construction does not diverge in logic.
     */
    public static RestTemplate buildRestTemplateWithTimeouts(int connectTimeoutMs, int readTimeoutMs) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(Math.max(0, connectTimeoutMs)));
        factory.setReadTimeout(Duration.ofMillis(Math.max(0, readTimeoutMs)));
        return new RestTemplate(factory);
    }
}
