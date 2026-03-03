package backend.configurations.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.stereotype.Component;

/**
 * Logs when a reCAPTCHA siteverify call is retried after 5xx or network error.
 * Message length is capped to avoid noisy logs from long upstream messages.
 */
@Component
public class RecaptchaRetryListener implements RetryListener {

    private static final Logger log = LoggerFactory.getLogger(RecaptchaRetryListener.class);
    private static final int MAX_MESSAGE_LENGTH = 200;

    @Override
    public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
        int nextAttempt = context.getRetryCount() + 1;
        String message = throwable.getMessage();
        String truncated = message == null ? "null" : (message.length() <= MAX_MESSAGE_LENGTH ? message : message.substring(0, MAX_MESSAGE_LENGTH) + "...");
        log.warn("reCAPTCHA siteverify retry scheduled (attempt {}) after error: {} - {}",
                nextAttempt,
                throwable.getClass().getSimpleName(),
                truncated);
        if (log.isDebugEnabled()) {
            log.debug("reCAPTCHA retry exception detail", throwable);
        }
    }
}
