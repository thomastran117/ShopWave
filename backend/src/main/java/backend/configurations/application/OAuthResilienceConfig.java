package backend.configurations.application;

import backend.configurations.environment.EnvironmentSetting;
import backend.security.oauth.InvalidOAuthTokenException;
import backend.security.oauth.OAuthRetryable;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.RetryListener;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.classify.BinaryExceptionClassifier;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.time.Duration;

/**
 * Retry and circuit breaker for OAuth token verification. Retries only on
 * transient failures (IO, 5xx, timeouts); does not retry invalid tokens.
 * Circuit breaker records transient failures and invalid tokens are ignored;
 * OAuthVerificationError (wrapped JVM/unexpected faults) is not ignored so
 * real faults are not masked and can open the circuit.
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

        BinaryExceptionClassifier classifier = new BinaryExceptionClassifier(
                OAuthRetryable.getTransientTypes(),
                false
        );
        classifier.setTraverseCauses(true);
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(
                retry.getMaxAttempts(),
                classifier
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
                .recordException(OAuthRetryable::isRetryable)
                .ignoreException(ex -> ex instanceof InvalidOAuthTokenException)
                .build();

        return CircuitBreaker.of("oauthVerification", config);
    }
}
