package backend.security.oauth;

/**
 * Thrown when an OAuth provider is not supported (e.g. Apple not yet implemented).
 * Allows handlers to return a specific status (e.g. 501) without exposing
 * a generic "Not implemented" message.
 */
public class OAuthNotSupportedException extends RuntimeException {

    public OAuthNotSupportedException(String message) {
        super(message);
    }
}
