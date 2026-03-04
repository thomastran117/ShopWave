package backend.configurations.application;

import backend.configurations.environment.EnvironmentSetting;
import backend.security.oauth.InvalidOAuthTokenException;
import backend.security.oauth.OAuthRetryable;
import backend.security.oauth.OAuthVerificationError;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.classify.Classifier;
import org.springframework.retry.RetryListener;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.ExceptionClassifierRetryPolicy;
import org.springframework.retry.policy.NeverRetryPolicy;
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

        // Use isRetryable() so HttpStatusCodeException is only retried when 5xx (same logic as circuit breaker)
        ExceptionClassifierRetryPolicy retryPolicy = new ExceptionClassifierRetryPolicy();
        retryPolicy.setExceptionClassifier((Classifier<Throwable, org.springframework.retry.RetryPolicy>) t ->
                OAuthRetryable.isRetryable(t) ? new SimpleRetryPolicy(retry.getMaxAttempts()) : new NeverRetryPolicy());
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
                .recordException(ex -> OAuthRetryable.isRetryable(OAuthRetryable.throwableForClassification(ex)))
                // Only ignore OAuthVerificationError when cause is explicitly non-retryable; null or retryable cause must count toward CB
                .ignoreException(ex -> ex instanceof InvalidOAuthTokenException
                        || (ex instanceof OAuthVerificationError o && o.getCause() != null && !OAuthRetryable.isRetryable(o.getCause())))
                .build();

        return CircuitBreaker.of("oauthVerification", config);
    }
}
