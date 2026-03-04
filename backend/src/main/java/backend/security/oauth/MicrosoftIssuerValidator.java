package backend.security.oauth;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Validates Microsoft identity platform v2.0 token issuers against the
 * recommended authority format and known tenant identifiers.
 *
 * @see <a href="https://learn.microsoft.com/en-us/entra/identity-platform/id-token-claims-reference">ID token claims</a>
 */
public final class MicrosoftIssuerValidator {

    private static final String AUTHORITY_HOST = "https://login.microsoftonline.com/";
    private static final String V2_PATH_SUFFIX = "/v2.0";
    private static final String V2_PATH_SUFFIX_SLASH = "/v2.0/";

    /** Microsoft-recommended well-known tenant segment values for v2.0. */
    private static final Set<String> WELL_KNOWN_TENANTS = Set.of(
            "common",        // multi-tenant / any
            "organizations", // work and school accounts
            "consumers"     // personal Microsoft accounts
    );

    /** Tenant GUID format (8-4-4-4-12 hex). */
    private static final Pattern TENANT_GUID = Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

    private MicrosoftIssuerValidator() {}

    /**
     * Returns true if the issuer is a valid Microsoft v2.0 issuer: authority host, then
     * either a well-known tenant (common, organizations, consumers) or a tenant GUID, then /v2.0.
     */
    public static boolean isValid(String issuer) {
        if (issuer == null || issuer.isBlank()) {
            return false;
        }
        if (!issuer.startsWith(AUTHORITY_HOST)) {
            return false;
        }
        String afterHost = issuer.substring(AUTHORITY_HOST.length());
        if (afterHost.endsWith(V2_PATH_SUFFIX)) {
            String tenant = afterHost.substring(0, afterHost.length() - V2_PATH_SUFFIX.length());
            return isValidTenantSegment(tenant);
        }
        if (afterHost.endsWith(V2_PATH_SUFFIX_SLASH)) {
            String tenant = afterHost.substring(0, afterHost.length() - V2_PATH_SUFFIX_SLASH.length());
            return isValidTenantSegment(tenant);
        }
        return false;
    }

    private static boolean isValidTenantSegment(String segment) {
        if (segment == null || segment.isBlank()) {
            return false;
        }
        if (WELL_KNOWN_TENANTS.contains(segment)) {
            return true;
        }
        return TENANT_GUID.matcher(segment).matches();
    }
}
