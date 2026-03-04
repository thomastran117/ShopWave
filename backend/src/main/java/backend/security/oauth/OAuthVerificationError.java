package backend.security.oauth;

/**
 * Wraps a JVM {@link Error} that occurred during OAuth verification so it can
 * cross boundaries that only declare {@link Exception}. Do not retry; do not
 * record in circuit breaker as a normal failure.
 */
public class OAuthVerificationError extends RuntimeException {

    public OAuthVerificationError(Error cause) {
        super(cause == null ? "OAuth verification failed (JVM error)" : cause.getMessage(), cause);
    }
}
