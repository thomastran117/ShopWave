package backend.configurations.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.stereotype.Component;

@Component
public class EmailRetryListener implements RetryListener {

    private static final Logger log = LoggerFactory.getLogger(EmailRetryListener.class);

    @Override
    public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
        int nextAttempt = context.getRetryCount() + 1;
        log.warn("Email send retry scheduled (attempt {}) after transient error: {} - {}",
                nextAttempt,
                throwable.getClass().getSimpleName(),
                throwable.getMessage());
        if (log.isDebugEnabled()) {
            log.debug("Email retry exception detail", throwable);
        }
    }

    @Override
    public <T, E extends Throwable> void onSuccess(RetryContext context, RetryCallback<T, E> callback, T result) {
        if (context.getRetryCount() > 0) {
            log.info("Email send succeeded after {} retries", context.getRetryCount());
        }
    }
}
