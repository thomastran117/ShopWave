package backend.security.oauth;

/**
 * Thrown when an OAuth token is invalid (bad signature, expired, wrong audience, etc.).
 * Not retried and not recorded by the circuit breaker.
 */
public class InvalidOAuthTokenException extends RuntimeException {

    public InvalidOAuthTokenException(String message) {
        super(message);
    }

    public InvalidOAuthTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
