package backend.services.intf;

import backend.models.other.OAuthUser;

import java.io.IOException;

/**
 * Verifies OAuth ID tokens from providers (Google, Microsoft). The client (web or mobile)
 * obtains the token from the provider and sends it to the backend; this service verifies
 * it with the provider (e.g. via signature validation and audience/issuer checks).
 */
public interface OAuthService {

    /**
     * Verify an Apple ID token. Not implemented.
     */
    OAuthUser verifyAppleToken(String appleToken);

    /**
     * Verify a Google ID token and return the user claims.
     *
     * @param googleToken the ID token from the Google sign-in client
     * @return verified user info (sub, email, name, provider)
     * @throws backend.exceptions.http.ServiceUnavaliableException if Google OAuth is not configured
     * @throws backend.exceptions.http.UnauthorizedException       if the token is invalid
     * @throws IOException                                        on transient network errors (caller may retry or rely on aspect retry)
     */
    OAuthUser verifyGoogleToken(String googleToken) throws IOException;

    /**
     * Verify a Microsoft ID token and return the user claims.
     *
     * @param microsoftToken the ID token from the Microsoft sign-in client
     * @return verified user info (sub, email, name, provider)
     * @throws backend.exceptions.http.ServiceUnavaliableException if Microsoft OAuth is not configured
     * @throws backend.exceptions.http.UnauthorizedException       if the token is invalid or required claims are missing
     */
    OAuthUser verifyMicrosoftToken(String microsoftToken);
}
