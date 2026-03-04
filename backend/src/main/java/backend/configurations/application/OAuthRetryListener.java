package backend.configurations.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Logs when an OAuth token verification call is retried after a transient error.
 * Logging is rate-limited to avoid log spam under failure loops (at most once per 10 seconds).
 */
@Component
public class OAuthRetryListener implements RetryListener {

    private static final Logger log = LoggerFactory.getLogger(OAuthRetryListener.class);
    private static final long RATE_LIMIT_MS = 10_000;

    private static final AtomicLong lastLogTime = new AtomicLong(0);

    private final OAuthMetrics oauthMetrics;

    public OAuthRetryListener(@Autowired(required = false) OAuthMetrics oauthMetrics) {
        this.oauthMetrics = oauthMetrics;
    }

    @Override
    public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
        int nextAttempt = context.getRetryCount() + 1;
        if (oauthMetrics != null) {
            oauthMetrics.recordRetry();
        }
        long now = System.currentTimeMillis();
        long prev = lastLogTime.get();
        if (now - prev >= RATE_LIMIT_MS && lastLogTime.compareAndSet(prev, now)) {
            log.warn("OAuth verification retry (attempt {}) after error: {}",
                    nextAttempt,
                    throwable.getClass().getSimpleName());
        }
        if (log.isTraceEnabled()) {
            log.trace("OAuth retry exception type: {}", throwable.getClass().getName());
        }
    }
}
