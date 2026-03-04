package backend.configurations.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.stereotype.Component;

/**
 * Logs when an OAuth token verification call is retried after a transient
 * error (e.g. network timeout, 5xx from provider). Only exception type is
 * logged; messages and stack traces are avoided in logs to prevent sensitive
 * or internal detail leakage.
 */
@Component
public class OAuthRetryListener implements RetryListener {

    private static final Logger log = LoggerFactory.getLogger(OAuthRetryListener.class);

    @Override
    public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
        int nextAttempt = context.getRetryCount() + 1;
        log.warn("OAuth verification retry scheduled (attempt {}) after error: {}",
                nextAttempt,
                throwable.getClass().getSimpleName());
        if (log.isTraceEnabled()) {
            log.trace("OAuth retry exception type: {}", throwable.getClass().getName());
        }
    }
}
