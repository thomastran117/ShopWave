package backend.security.oauth;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Validates Microsoft identity platform v2.0 token issuers against a configurable
 * authority host and tenant set. Use {@link #isValid(String, String, Set)} for
 * sovereign clouds or custom hosts (e.g. login.microsoftonline.de).
 * <p>
 * Limitation: Only the public Azure authority and configurable host/tenant list
 * are supported; multi-cloud or custom issuer patterns require custom validation.
 *
 * @see <a href="https://learn.microsoft.com/en-us/entra/identity-platform/id-token-claims-reference">ID token claims</a>
 */
public final class MicrosoftIssuerValidator {

    private static final String DEFAULT_AUTHORITY_HOST = "https://login.microsoftonline.com/";
    private static final String V2_PATH_SUFFIX = "/v2.0";
    private static final String V2_PATH_SUFFIX_SLASH = "/v2.0/";

    /** Default well-known tenant segment values for public Azure v2.0. */
    private static final Set<String> DEFAULT_WELL_KNOWN_TENANTS = Set.of(
            "common", "organizations", "consumers"
    );

    private static final Pattern TENANT_GUID = Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

    private MicrosoftIssuerValidator() {}

    /**
     * Validates using default public Azure authority host and well-known tenants.
     * For sovereign clouds (e.g. Azure Germany), use {@link #isValid(String, String, Set)}.
     */
    public static boolean isValid(String issuer) {
        return isValid(issuer, DEFAULT_AUTHORITY_HOST, DEFAULT_WELL_KNOWN_TENANTS);
    }

    /**
     * Validates issuer against the given authority host and allowed well-known tenant segments.
     * Authority host must end with / (e.g. https://login.microsoftonline.com/ or
     * https://login.microsoftonline.de/ for sovereign clouds). Well-known tenants are
     * segment values between host and /v2.0 (e.g. common, organizations, consumers);
     * any tenant GUID is always allowed.
     */
    public static boolean isValid(String issuer, String authorityHost, Set<String> wellKnownTenants) {
        if (issuer == null || issuer.isBlank()) {
            return false;
        }
        String host = (authorityHost != null && !authorityHost.isBlank()) ? authorityHost : DEFAULT_AUTHORITY_HOST;
        if (!host.endsWith("/")) {
            host = host + "/";
        }
        if (!issuer.startsWith(host)) {
            return false;
        }
        String afterHost = issuer.substring(host.length());
        Set<String> tenants = (wellKnownTenants != null && !wellKnownTenants.isEmpty())
                ? wellKnownTenants : DEFAULT_WELL_KNOWN_TENANTS;
        if (afterHost.endsWith(V2_PATH_SUFFIX)) {
            String tenant = afterHost.substring(0, afterHost.length() - V2_PATH_SUFFIX.length());
            return isValidTenantSegment(tenant, tenants);
        }
        if (afterHost.endsWith(V2_PATH_SUFFIX_SLASH)) {
            String tenant = afterHost.substring(0, afterHost.length() - V2_PATH_SUFFIX_SLASH.length());
            return isValidTenantSegment(tenant, tenants);
        }
        return false;
    }

    private static boolean isValidTenantSegment(String segment, Set<String> wellKnownTenants) {
        if (segment == null || segment.isBlank()) {
            return false;
        }
        if (wellKnownTenants.contains(segment)) {
            return true;
        }
        return TENANT_GUID.matcher(segment).matches();
    }
}
