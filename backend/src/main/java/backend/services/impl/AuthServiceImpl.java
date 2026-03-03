package backend.services.impl;

import backend.models.core.User;
import backend.services.intf.AuthService;
import backend.services.intf.TokenService;
import backend.services.intf.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final TokenService tokenService;

    public AuthServiceImpl(UserService userService, TokenService tokenService) {
        this.userService = userService;
        this.tokenService = tokenService;
    }

    @Override
    public LoginResult login(String email, String password) {
        User user = userService.login(email, password);
        Map<String, Object> tokens = tokenService.generateTokenPair(user.getId().intValue(), user.getRole().toString());
        String accessToken = (String) tokens.get("accessToken");
        String refreshToken = (String) tokens.get("refreshToken");
        return new LoginResult(accessToken, refreshToken, user.getEmail(), user.getRole().toString(), user.getId());
    }

    @Override
    public RefreshResult refresh(String refreshToken) {
        if (refreshToken == null || !tokenService.validateRefreshToken(refreshToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired refresh token");
        }
        TokenService.RefreshTokenPayload payload = tokenService.getRefreshTokenPayload(refreshToken);
        if (payload == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired refresh token");
        }

        String newRefreshToken = tokenService.rotateRefreshToken(refreshToken);
        if (newRefreshToken == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired refresh token");
        }

        String newAccessToken = tokenService.generateAccessToken(payload.userId(), payload.role());
        long expiresIn = tokenService.getAccessTokenExpiresInSeconds();

        return new RefreshResult(newAccessToken, newRefreshToken, expiresIn);
    }

    @Override
    public LoginResult loginOrSignupGoogle(String email) {
        User user = userService.loginOrSignupGoogle(email);
        Map<String, Object> tokens = tokenService.generateTokenPair(user.getId().intValue(), user.getRole().toString());
        String accessToken = (String) tokens.get("accessToken");
        String refreshToken = (String) tokens.get("refreshToken");
        return new LoginResult(accessToken, refreshToken, user.getEmail(), user.getRole().toString(), user.getId());
    }

    @Override
    public void revokeRefreshToken(String refreshToken) {
        tokenService.revokeRefreshToken(refreshToken);
    }

    @Override
    public void revokeAllRefreshTokensForUser(int userId) {
        tokenService.revokeAllRefreshTokensForUser(userId);
    }
}
