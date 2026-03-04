package backend.security.oauth;

import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.io.IOException;
import java.util.Set;

/**
 * Single source of truth for which exceptions are considered transient
 * (retryable) in OAuth verification. Used by the service layer and by
 * retry/circuit-breaker configuration.
 */
public final class OAuthRetryable {

    @SuppressWarnings("unchecked")
    private static final Set<Class<? extends Throwable>> TRANSIENT_TYPES = Set.of(
            (Class<? extends Throwable>) (Class<?>) OAuthProviderTransientException.class,
            (Class<? extends Throwable>) (Class<?>) IOException.class,
            (Class<? extends Throwable>) (Class<?>) HttpServerErrorException.class,
            (Class<? extends Throwable>) (Class<?>) ResourceAccessException.class
    );

    private OAuthRetryable() {}

    /**
     * Returns the set of exception types considered transient (retryable).
     * Use this in retry/circuit-breaker configuration to stay in sync with
     * {@link #isRetryable(Throwable)} and avoid divergence.
     */
    public static Set<Class<? extends Throwable>> getTransientTypes() {
        return Set.copyOf(TRANSIENT_TYPES);
    }

    /**
     * Returns true if the throwable (or its cause chain) represents a
     * transient failure that should be retried.
     */
    public static boolean isRetryable(Throwable t) {
        if (t == null) {
            return false;
        }
        Throwable current = t;
        while (current != null) {
            for (Class<?> type : TRANSIENT_TYPES) {
                if (type.isInstance(current)) {
                    return true;
                }
            }
            current = current.getCause();
        }
        return false;
    }
}
