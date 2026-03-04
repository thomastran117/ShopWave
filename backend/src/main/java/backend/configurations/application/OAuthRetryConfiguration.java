package backend.configurations.application;

import backend.configurations.environment.EnvironmentSetting;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.io.IOException;
import java.time.Duration;

/**
 * Retry and circuit breaker configuration for OAuth token verification
 * (Google/Microsoft). Retries on transient network/HTTP errors; does not retry
 * on auth failures (e.g. UnauthorizedException). Circuit breaker opens after
 * repeated failures to avoid hammering the provider.
 */
@Configuration
public class OAuthRetryConfiguration {

    @Bean("oauthRetryTemplate")
    @Qualifier("oauthRetryTemplate")
    public RetryTemplate oauthRetryTemplate(
            EnvironmentSetting env,
            OAuthRetryListener oauthRetryListener) {
        EnvironmentSetting.OAuth.Retry retry = env.getOauth().getRetry();
        return RetryTemplate.builder()
                .maxAttempts(retry.getMaxAttempts())
                .exponentialBackoff(
                        retry.getInitialIntervalMs(),
                        retry.getMultiplier(),
                        retry.getMaxIntervalMs())
                .retryOn(IOException.class)
                .retryOn(HttpServerErrorException.class)
                .retryOn(ResourceAccessException.class)
                .traversingCauses()
                .withListener(oauthRetryListener)
                .build();
    }

    @Bean("oauthCircuitBreaker")
    @Qualifier("oauthCircuitBreaker")
    public CircuitBreaker oauthCircuitBreaker(EnvironmentSetting env) {
        EnvironmentSetting.OAuth.CircuitBreaker cb = env.getOauth().getCircuitBreaker();
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(cb.getFailureRateThreshold())
                .waitDurationInOpenState(Duration.ofSeconds(cb.getWaitDurationInOpenStateSeconds()))
                .slidingWindowSize(cb.getSlidingWindowSize())
                .minimumNumberOfCalls(cb.getMinimumNumberOfCalls())
                .build();
        return CircuitBreaker.of("oauth", config);
    }
}
