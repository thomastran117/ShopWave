package backend.services.intf;

/**
 * Handles authentication flows (login, refresh). Delegates JWT operations to {@link TokenService}
 * and user operations to {@link UserService}.
 */
public interface AuthService {

    /**
     * Result of a successful login: tokens and user info for the response.
     */
    record LoginResult(String accessToken, String refreshToken, String email, String usertype, long userId) {}

    /**
     * Result of a successful token refresh: new access token and refresh token.
     */
    record RefreshResult(String accessToken, String refreshToken, long expiresInSeconds) {}

    /**
     * Authenticate user with email/password and produce token pair.
     */
    LoginResult localAuthenicate(String email, String password);

    /**
     * Validate refresh token and issue new access + refresh token pair.
     */
    RefreshResult refresh(String refreshToken);

    /**
     * Authenticate via Google (verify id token, then login or signup) and produce token pair.
     */
    LoginResult googleAuthenicate(String token);

    /**
     * Authenticate via Microsoft (verify id token, then login or signup) and produce token pair.
     */
    LoginResult microsoftAuthenticate(String token);

    /**
     * Authenticate via Apple (verify id token, then login or signup) and produce token pair.
     */
    LoginResult appleAuthenticate(String token);

    /**
     * Revoke a single refresh token (e.g. logout). No-op if token already invalid.
     */
    void revokeRefreshToken(String refreshToken);

    /**
     * Revoke all refresh tokens for a user (e.g. security reset, password change).
     */
    void revokeAllRefreshTokensForUser(int userId);
}
