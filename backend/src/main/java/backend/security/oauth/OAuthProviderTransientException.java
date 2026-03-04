package backend.security.oauth;

/**
 * Thrown when an OAuth provider call fails transiently (timeout, IO, 5xx, rate limit).
 * Retried by the retry policy and recorded by the circuit breaker.
 */
public class OAuthProviderTransientException extends RuntimeException {

    public OAuthProviderTransientException(String message) {
        super(message);
    }

    public OAuthProviderTransientException(String message, Throwable cause) {
        super(message, cause);
    }
}
