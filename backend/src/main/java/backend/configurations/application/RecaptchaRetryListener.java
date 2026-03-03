package backend.configurations.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.stereotype.Component;

/**
 * Logs when a reCAPTCHA siteverify call is retried after 5xx or network error.
 */
@Component
public class RecaptchaRetryListener implements RetryListener {

    private static final Logger log = LoggerFactory.getLogger(RecaptchaRetryListener.class);

    @Override
    public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
        int nextAttempt = context.getRetryCount() + 1;
        log.warn("reCAPTCHA siteverify retry scheduled (attempt {}) after error: {} - {}",
                nextAttempt,
                throwable.getClass().getSimpleName(),
                throwable.getMessage());
        if (log.isDebugEnabled()) {
            log.debug("reCAPTCHA retry exception detail", throwable);
        }
    }
}
