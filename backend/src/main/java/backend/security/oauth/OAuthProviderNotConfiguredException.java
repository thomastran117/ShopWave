package backend.security.oauth;

/**
 * Thrown when a request is made to an OAuth provider that has not been configured
 * (i.e. its client ID environment variable is missing). Not retried and not counted
 * by the circuit breaker — this is a server-side configuration issue, not a
 * transient provider failure.
 */
public class OAuthProviderNotConfiguredException extends RuntimeException {

    public OAuthProviderNotConfiguredException(String message) {
        super(message);
    }
}
