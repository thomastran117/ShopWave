package backend.configurations.application;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.stereotype.Component;

/**
 * Logs when an OAuth token verification call is retried after a transient
 * error (e.g. network timeout, 5xx from provider).
 */
@Component
public class OAuthRetryListener implements RetryListener {

    private static final Logger log = LoggerFactory.getLogger(OAuthRetryListener.class);
    private static final int MAX_MESSAGE_LENGTH = 200;

    @Override
    public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
        int nextAttempt = context.getRetryCount() + 1;
        String message = throwable.getMessage();
        String abbreviated = message == null ? "null" : StringUtils.abbreviate(message, MAX_MESSAGE_LENGTH);
        log.warn("OAuth verification retry scheduled (attempt {}) after error: {} - {}",
                nextAttempt,
                throwable.getClass().getSimpleName(),
                abbreviated);
        if (log.isDebugEnabled()) {
            log.debug("OAuth retry exception detail", throwable);
        }
    }
}
