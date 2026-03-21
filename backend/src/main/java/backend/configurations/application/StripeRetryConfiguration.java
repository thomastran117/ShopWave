package backend.configurations.application;

import com.stripe.exception.ApiConnectionException;
import com.stripe.exception.ApiException;
import com.stripe.exception.RateLimitException;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.classify.Classifier;
import org.springframework.retry.RetryListener;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.ExceptionClassifierRetryPolicy;
import org.springframework.retry.policy.NeverRetryPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import backend.configurations.environment.EnvironmentSetting;

/**
 * Retry configuration for Stripe API calls. Only retries on transient errors:
 * connection failures, 5xx server errors, and rate-limit (429). Card declines,
 * auth errors, and invalid-request errors are NOT retried.
 */
@Configuration
public class StripeRetryConfiguration {

    private final EnvironmentSetting env;
    private final StripeRetryListener stripeRetryListener;

    public StripeRetryConfiguration(EnvironmentSetting env, StripeRetryListener stripeRetryListener) {
        this.env = env;
        this.stripeRetryListener = stripeRetryListener;
    }

    @Bean("stripeRetryTemplate")
    @Qualifier("stripeRetryTemplate")
    public RetryTemplate stripeRetryTemplate() {
        EnvironmentSetting.Stripe.Retry retry = env.getStripe().getRetry();

        ExponentialBackOffPolicy backOff = new ExponentialBackOffPolicy();
        backOff.setInitialInterval(retry.getInitialIntervalMs());
        backOff.setMultiplier(retry.getMultiplier());
        backOff.setMaxInterval(retry.getMaxIntervalMs());

        ExceptionClassifierRetryPolicy retryPolicy = new ExceptionClassifierRetryPolicy();
        retryPolicy.setExceptionClassifier((Classifier<Throwable, RetryPolicy>) t -> {
            if (isTransientStripeError(t)) {
                return new SimpleRetryPolicy(retry.getMaxAttempts());
            }
            return new NeverRetryPolicy();
        });

        RetryTemplate template = new RetryTemplate();
        template.setBackOffPolicy(backOff);
        template.setRetryPolicy(retryPolicy);
        template.setListeners(new RetryListener[]{ stripeRetryListener });
        return template;
    }

    /**
     * Determines whether the exception is a transient Stripe error worth retrying.
     * Walks the cause chain to find Stripe exceptions wrapped by Spring retry.
     */
    private static boolean isTransientStripeError(Throwable t) {
        Throwable current = t;
        while (current != null) {
            if (current instanceof ApiConnectionException) return true;
            if (current instanceof RateLimitException) return true;
            if (current instanceof ApiException apiEx && !(current instanceof com.stripe.exception.CardException)
                    && !(current instanceof com.stripe.exception.AuthenticationException)
                    && !(current instanceof com.stripe.exception.InvalidRequestException)) {
                Integer statusCode = apiEx.getStatusCode();
                if (statusCode != null && statusCode >= 500) return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
