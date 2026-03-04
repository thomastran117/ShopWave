package backend.security.oauth;

/**
 * Wraps a JVM {@link Error} or other non-Exception throwable that occurred during
 * OAuth verification so it can cross boundaries that only declare {@link Exception}.
 * Do not retry; do not record in circuit breaker as a normal failure.
 */
public class OAuthVerificationError extends RuntimeException {

    public OAuthVerificationError(Error cause) {
        super(cause == null ? "OAuth verification failed (JVM error)" : cause.getMessage(), cause);
    }

    /** Preserves message and cause for any throwable to avoid silent failure. */
    public OAuthVerificationError(String message, Throwable cause) {
        super(message != null ? message : (cause != null ? cause.getMessage() : "OAuth verification failed"), cause);
    }
}
