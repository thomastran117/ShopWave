package backend.security.oauth;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpTransport;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Resolves Google's recommended issuer list from the OpenID Connect discovery document
 * so verification stays valid as Google's domains evolve. Falls back to a known default
 * if discovery is unreachable (e.g. at startup in restricted environments).
 */
public final class GoogleDiscoveryIssuers {

    private static final Logger log = LoggerFactory.getLogger(GoogleDiscoveryIssuers.class);
    private static final String DISCOVERY_URL = "https://accounts.google.com/.well-known/openid-configuration";
    /** Fallback when discovery fetch fails (e.g. network restriction at startup). */
    private static final List<String> FALLBACK_ISSUERS = List.of(
            "https://accounts.google.com",
            "accounts.google.com"
    );

    private GoogleDiscoveryIssuers() {}

    /**
     * Returns the issuer list to use for Google ID token verification. Fetches from
     * Google's discovery document; on failure returns a safe fallback list.
     *
     * @param transport HTTP transport used for the discovery request (same timeouts as cert fetch)
     * @return non-empty list of issuer strings (main issuer first; legacy forms included when applicable)
     */
    public static List<String> getIssuers(HttpTransport transport) {
        if (transport == null) {
            return new ArrayList<>(FALLBACK_ISSUERS);
        }
        try {
            HttpRequestFactory requestFactory = transport.createRequestFactory();
            String body = requestFactory.buildGetRequest(new GenericUrl(DISCOVERY_URL))
                    .execute()
                    .parseAsString();
            return parseIssuersFromDiscovery(body);
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Google discovery fetch failed, using fallback issuers: {}", e.getMessage());
            }
            return new ArrayList<>(FALLBACK_ISSUERS);
        }
    }

    /**
     * Parses the discovery JSON and returns a list of issuers. Uses the standard "issuer"
     * field; adds the legacy form without scheme if different so older tokens still verify.
     */
    @SuppressWarnings("unused")
    static List<String> parseIssuersFromDiscovery(String discoveryJson) {
        List<String> issuers = new ArrayList<>();
        if (discoveryJson == null || discoveryJson.isBlank()) {
            issuers.addAll(FALLBACK_ISSUERS);
            return issuers;
        }
        try {
            JsonObject obj = new Gson().fromJson(discoveryJson, JsonObject.class);
            if (obj != null && obj.has("issuer")) {
                String issuer = obj.get("issuer").getAsString();
                if (issuer != null && !issuer.isBlank()) {
                    issuers.add(issuer.trim());
                    String withoutScheme = issuer.replaceFirst("^https?://", "").trim();
                    if (!withoutScheme.isEmpty() && !issuers.contains(withoutScheme)) {
                        issuers.add(withoutScheme);
                    }
                }
            }
        } catch (Exception e) {
            if (log.isTraceEnabled()) {
                log.trace("Failed to parse Google discovery JSON", e);
            }
        }
        if (issuers.isEmpty()) {
            issuers.addAll(FALLBACK_ISSUERS);
        }
        return issuers;
    }
}
