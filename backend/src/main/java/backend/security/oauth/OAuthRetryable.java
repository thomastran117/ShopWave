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

    private static final Set<Class<?>> TRANSIENT_TYPES = Set.of(
            OAuthProviderTransientException.class,
            IOException.class,
            HttpServerErrorException.class,
            ResourceAccessException.class
    );

    private OAuthRetryable() {}

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
