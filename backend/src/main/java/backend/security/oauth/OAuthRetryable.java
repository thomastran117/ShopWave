package backend.security.oauth;

import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;

import java.io.IOException;
import java.util.Set;

/**
 * Single source of truth for which exceptions are considered transient
 * (retryable) in OAuth verification. Uses explicit type checks only (no
 * class-name substring); HttpStatusCodeException is retryable only when
 * status is 5xx. Used by {@link OAuthExceptionClassifier}, retry and
 * circuit-breaker configuration.
 */
public final class OAuthRetryable {

    @SuppressWarnings("unchecked")
    private static final Set<Class<? extends Throwable>> TRANSIENT_TYPES = Set.of(
            (Class<? extends Throwable>) (Class<?>) OAuthProviderTransientException.class,
            (Class<? extends Throwable>) (Class<?>) IOException.class,
            (Class<? extends Throwable>) (Class<?>) HttpStatusCodeException.class,
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
        return isRetryableForClassification(throwableForClassification(t));
    }

    private static boolean isRetryableForClassification(Throwable t) {
        if (t == null) {
            return false;
        }
        Throwable current = t;
        while (current != null) {
            for (Class<?> type : TRANSIENT_TYPES) {
                if (type.isInstance(current)) {
                    // HttpStatusCodeException: only 5xx are retryable; 4xx are not
                    if (current instanceof HttpStatusCodeException h) {
                        HttpStatusCode status = h.getStatusCode();
                        if (status == null || status.value() < 500 || status.value() >= 600) {
                            continue;
                        }
                    }
                    return true;
                }
            }
            current = current.getCause();
        }
        return false;
    }

    /**
     * For Resilience4j: use root cause for classification when the throwable is
     * OAuthVerificationError (wrapped), so retry/circuit breaker see the real type.
     */
    public static Throwable throwableForClassification(Throwable t) {
        if (t == null) {
            return null;
        }
        if (t instanceof OAuthVerificationError o && o.getCause() != null) {
            return o.getCause();
        }
        return t;
    }
}
