package backend.configurations.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.stereotype.Component;

@Component
public class StripeRetryListener implements RetryListener {

    private static final Logger log = LoggerFactory.getLogger(StripeRetryListener.class);

    @Override
    public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
        int nextAttempt = context.getRetryCount() + 1;
        log.warn("Stripe retry scheduled (attempt {}) after transient error: {} - {}",
                nextAttempt,
                throwable.getClass().getSimpleName(),
                throwable.getMessage());
        if (log.isDebugEnabled()) {
            log.debug("Stripe retry exception detail", throwable);
        }
    }
}
