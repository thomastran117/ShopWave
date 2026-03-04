package backend.security.oauth;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

/**
 * Shared claim extraction for OAuth tokens. Extracts string claims with
 * preferred/fallback keys for testability and reuse.
 */
public final class OAuthClaimUtils {

    private OAuthClaimUtils() {}

    /**
     * Gets a string claim from a JWT, trying the preferred key first, then the fallback if non-null.
     *
     * @param jwt       the decoded JWT
     * @param preferred primary claim name (e.g. "preferred_username")
     * @param fallback  optional fallback claim name (e.g. "email"), or null
     * @return the claim value, or null if missing or blank
     */
    public static String getClaim(Jwt jwt, String preferred, String fallback) {
        return Optional.ofNullable(jwt.getClaim(preferred)).map(Object::toString).filter(s -> !s.isBlank())
                .or(() -> Optional.ofNullable(fallback).map(f -> jwt.getClaim(f)).map(Object::toString).filter(s -> !s.isBlank()))
                .orElse(null);
    }
}
