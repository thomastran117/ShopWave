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

    private static final int DEFAULT_RETRY_ATTEMPTS = 4;
    private static final long DEFAULT_INITIAL_INTERVAL_MS = 200;
    private static final double DEFAULT_MULTIPLIER = 2.0;
    private static final long DEFAULT_MAX_INTERVAL_MS = 10_000;
    private static final float DEFAULT_CB_FAILURE_RATE = 50f;
    private static final int DEFAULT_CB_WAIT_SECONDS = 60;
    private static final int DEFAULT_CB_WINDOW_SIZE = 10;
    private static final int DEFAULT_CB_MIN_CALLS = 5;

    @Bean("oauthRetryTemplate")
    @Qualifier("oauthRetryTemplate")
    public RetryTemplate oauthRetryTemplate() {
        EnvironmentSetting.OAuth oauth = env != null ? env.getOauth() : null;
        EnvironmentSetting.OAuth.Retry retry = (oauth != null && oauth.getRetry() != null) ? oauth.getRetry() : null;
        int maxAttempts = retry != null ? retry.getMaxAttempts() : DEFAULT_RETRY_ATTEMPTS;
        long initialMs = retry != null ? retry.getInitialIntervalMs() : DEFAULT_INITIAL_INTERVAL_MS;
        double multiplier = retry != null ? retry.getMultiplier() : DEFAULT_MULTIPLIER;
        long maxIntervalMs = retry != null ? retry.getMaxIntervalMs() : DEFAULT_MAX_INTERVAL_MS;

        RetryTemplate template = new RetryTemplate();
        ExponentialBackOffPolicy backOff = new ExponentialBackOffPolicy();
        backOff.setInitialInterval(initialMs);
        backOff.setMultiplier(multiplier);
        backOff.setMaxInterval(maxIntervalMs);
        template.setBackOffPolicy(backOff);

        ExceptionClassifierRetryPolicy retryPolicy = new ExceptionClassifierRetryPolicy();
        retryPolicy.setExceptionClassifier((Classifier<Throwable, org.springframework.retry.RetryPolicy>) t ->
                OAuthRetryable.isRetryable(t) ? new SimpleRetryPolicy(maxAttempts) : new NeverRetryPolicy());
        template.setRetryPolicy(retryPolicy);
        template.setListeners(new RetryListener[]{ oauthRetryListener });
        return template;
    }

    @Bean("oauthCircuitBreaker")
    @Qualifier("oauthCircuitBreaker")
    public CircuitBreaker oauthCircuitBreaker() {
        EnvironmentSetting.OAuth oauth = env != null ? env.getOauth() : null;
        EnvironmentSetting.OAuth.CircuitBreaker cb = (oauth != null && oauth.getCircuitBreaker() != null) ? oauth.getCircuitBreaker() : null;
        float failureRate = cb != null ? cb.getFailureRateThreshold() : DEFAULT_CB_FAILURE_RATE;
        int waitSec = cb != null ? cb.getWaitDurationInOpenStateSeconds() : DEFAULT_CB_WAIT_SECONDS;
        int windowSize = cb != null ? cb.getSlidingWindowSize() : DEFAULT_CB_WINDOW_SIZE;
        int minCalls = cb != null ? cb.getMinimumNumberOfCalls() : DEFAULT_CB_MIN_CALLS;

        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(failureRate)
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(windowSize)
                .minimumNumberOfCalls(minCalls)
                .waitDurationInOpenState(Duration.ofSeconds(waitSec))
                .recordException(ex -> OAuthRetryable.isRetryable(OAuthRetryable.throwableForClassification(ex)))
                // Only ignore OAuthVerificationError when cause is explicitly non-retryable; null or retryable cause must count toward CB
                .ignoreException(ex -> ex instanceof InvalidOAuthTokenException
                        || (ex instanceof OAuthVerificationError o && o.getCause() != null && !OAuthRetryable.isRetryable(o.getCause())))
                .build();

        return CircuitBreaker.of("oauthVerification", config);
    }
}
