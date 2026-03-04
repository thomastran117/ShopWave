package backend.configurations.application;

import backend.configurations.environment.EnvironmentSetting;
import backend.security.oauth.InvalidOAuthTokenException;
import backend.security.oauth.OAuthProviderTransientException;
import backend.security.oauth.OAuthVerificationError;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.RetryListener;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Retry and circuit breaker for OAuth token verification. Retries only on
 * transient failures (IO, 5xx, timeouts); does not retry invalid tokens.
 * Circuit breaker records only transient failures and ignores invalid token
 * failures so client errors do not open the circuit.
 */
@Configuration
public class OAuthResilienceConfig {

    private final EnvironmentSetting env;
    private final OAuthRetryListener oauthRetryListener;

    public OAuthResilienceConfig(EnvironmentSetting env, OAuthRetryListener oauthRetryListener) {
        this.env = env;
        this.oauthRetryListener = oauthRetryListener;
    }

    @Bean("oauthRetryTemplate")
    @Qualifier("oauthRetryTemplate")
    public RetryTemplate oauthRetryTemplate() {
        EnvironmentSetting.OAuth.Retry retry = env.getOauth().getRetry();

        RetryTemplate template = new RetryTemplate();

        ExponentialBackOffPolicy backOff = new ExponentialBackOffPolicy();
        backOff.setInitialInterval(retry.getInitialIntervalMs());
        backOff.setMultiplier(retry.getMultiplier());
        backOff.setMaxInterval(retry.getMaxIntervalMs());
        template.setBackOffPolicy(backOff);

        Map<Class<? extends Throwable>, Boolean> retryable = new HashMap<>();
        retryable.put(OAuthProviderTransientException.class, true);
        retryable.put(IOException.class, true);
        retryable.put(HttpServerErrorException.class, true);
        retryable.put(ResourceAccessException.class, true);
        retryable.put(InvalidOAuthTokenException.class, false);
        retryable.put(OAuthVerificationError.class, false);

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(
                retry.getMaxAttempts(),
                retryable,
                true
        );
        template.setRetryPolicy(retryPolicy);
        template.setListeners(new RetryListener[]{ oauthRetryListener });

        return template;
    }

    @Bean("oauthCircuitBreaker")
    @Qualifier("oauthCircuitBreaker")
    public CircuitBreaker oauthCircuitBreaker() {
        EnvironmentSetting.OAuth.CircuitBreaker cb = env.getOauth().getCircuitBreaker();

        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(cb.getFailureRateThreshold())
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(cb.getSlidingWindowSize())
                .minimumNumberOfCalls(cb.getMinimumNumberOfCalls())
                .waitDurationInOpenState(Duration.ofSeconds(cb.getWaitDurationInOpenStateSeconds()))
                .recordException(ex -> ex instanceof OAuthProviderTransientException
                        || ex instanceof IOException
                        || ex instanceof HttpServerErrorException
                        || ex instanceof ResourceAccessException)
                .ignoreException(ex -> ex instanceof InvalidOAuthTokenException || ex instanceof OAuthVerificationError)
                .build();

        return CircuitBreaker.of("oauth", config);
    }
}
