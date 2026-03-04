package backend.security.oauth;

import org.springframework.security.oauth2.jwt.Jwt;

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
        String value = getClaimValue(jwt, preferred);
        if (value != null && !value.isBlank()) {
            return value;
        }
        if (fallback != null) {
            value = getClaimValue(jwt, fallback);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private static String getClaimValue(Jwt jwt, String name) {
        Object claim = jwt.getClaim(name);
        if (claim == null) {
            return null;
        }
        String s = claim.toString();
        return s.isBlank() ? null : s;
    }
}
