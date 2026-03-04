package backend.security.oauth;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.Map;

/**
 * Shared claim extraction for OAuth tokens. Extracts string claims with
 * preferred/fallback keys. Validates claim types (String vs Object) to avoid ClassCast issues.
 */
public final class OAuthClaimUtils {

    private OAuthClaimUtils() {}

    /**
     * Gets a string claim from a JWT, trying the preferred key first, then the fallback if non-null.
     * Only values that are strings or safely stringifiable (Number, Boolean) are returned;
     * complex types (Map, List, etc.) are treated as missing to avoid ClassCastException.
     *
     * @param jwt       the decoded JWT
     * @param preferred primary claim name (e.g. "preferred_username")
     * @param fallback  optional fallback claim name (e.g. "email"), or null
     * @return the claim value, or null if missing, blank, or not a string-like type
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

    /**
     * Extracts a string value from a claim. Handles known claim shapes: String, Number, Boolean,
     * and collection-based claims (e.g. single-element list of string) common for some Microsoft tenants.
     * Map and other complex types are not stringified. Ensures coverage of required claims when
     * provider sends them as collections.
     */
    private static String getClaimValue(Jwt jwt, String name) {
        Object claim = jwt.getClaim(name);
        if (claim == null) {
            return null;
        }
        if (claim instanceof String s) {
            return s.isBlank() ? null : s;
        }
        if (claim instanceof Number || claim instanceof Boolean) {
            String s = claim.toString();
            return s == null || s.isBlank() ? null : s;
        }
        // Collection-based claim (e.g. ["user@example.com"]): take first non-blank string element
        if (claim instanceof Collection<?> c) {
            for (Object o : c) {
                if (o instanceof String s && !s.isBlank()) {
                    return s;
                }
                if (o != null) {
                    String s = o.toString();
                    if (s != null && !s.isBlank()) {
                        return s;
                    }
                }
            }
            return null;
        }
        // Map or other: do not coerce to avoid wrong or unsafe string representation
        if (claim instanceof Map) {
            return null;
        }
        return null;
    }
}
