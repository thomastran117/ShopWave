package backend.models.other;

/**
 * Represents a user authenticated via an OAuth provider (Google, Microsoft, etc.).
 * The client obtains an ID token from the provider and sends it to the backend for verification.
 */
public record OAuthUser(
        String sub,
        String email,
        String name,
        String provider
) {}
