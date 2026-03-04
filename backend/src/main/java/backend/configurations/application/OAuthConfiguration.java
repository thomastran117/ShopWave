package backend.configurations.application;

import backend.configurations.environment.EnvironmentSetting;
import backend.security.oauth.MicrosoftIssuerValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * OAuth configuration layer: validates required OAuth settings (Google/Microsoft client IDs,
 * Microsoft JWKS URI) at startup and provides a cached Microsoft JwtDecoder with timeouts
 * and optional JWKS reachability check. Validation happens here instead of in the service
 * constructor to keep startup failures in the configuration layer.
 */
@Configuration
@DependsOn("oauthConfigValidator")
public class OAuthConfiguration {

    private static final Logger log = LoggerFactory.getLogger(OAuthConfiguration.class);

    private static final String HEADER_TYP_ACCESS_TOKEN = "at+jwt";
    private static final String HEADER_ALG_NONE = "none";

    /**
     * Single cached JwtDecoder for Microsoft ID tokens. Uses a dedicated RestTemplate
     * with connect/read timeouts for JWKS fetching to avoid blocking threads.
     * JWKS URI is validated for reachability at startup when possible.
     */
    @Bean("microsoftJwtDecoder")
    @Qualifier("microsoftJwtDecoder")
    public JwtDecoder microsoftJwtDecoder(EnvironmentSetting env) {
        EnvironmentSetting.Security security = env.getSecurity();
        String googleId = security.getGoogleClientId();
        String microsoftId = security.getMicrosoftClientId();
        String jwksUri = security.getMicrosoftJwksUri();

        if (googleId == null || googleId.isBlank()) {
            throw new IllegalStateException(
                    "Google OAuth client ID is not configured (set app.security.google-client-id)");
        }
        if (microsoftId == null || microsoftId.isBlank()) {
            throw new IllegalStateException(
                    "Microsoft OAuth client ID is not configured (set app.security.microsoft-client-id)");
        }
        if (jwksUri == null || jwksUri.isBlank()) {
            throw new IllegalStateException(
                    "Microsoft OAuth JWKS URI is not configured (set app.security.microsoft-jwks-uri)");
        }

        RestTemplate jwksRest = jwksRestTemplate(env);
        if (security.getJwks().isValidateAtStartup()) {
            validateJwksReachable(jwksUri, jwksRest, security.getJwks());
        }

        String authorityHost = security.getMicrosoftAuthorityHost();
        Set<String> wellKnownTenants = parseWellKnownTenants(security.getMicrosoftWellKnownTenants());
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwksUri)
                .restOperations(jwksRest)
                .build();
        decoder.setJwtValidator(microsoftTokenValidator(microsoftId, authorityHost, wellKnownTenants));
        return decoder;
    }

    private static RestTemplate jwksRestTemplate(EnvironmentSetting env) {
        EnvironmentSetting.Security.Jwks jwks = env.getSecurity().getJwks();
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(jwks.getConnectTimeoutMs()));
        factory.setReadTimeout(Duration.ofMillis(jwks.getReadTimeoutMs()));
        return new RestTemplate(factory);
    }

    /**
     * When app.security.jwks.validate-at-startup is true, validates JWKS URI is reachable.
     * If app.security.jwks.fail-startup-on-unreachable is false (default), logs warning and continues so non-prod does not block.
     */
    private static void validateJwksReachable(String jwksUri, RestTemplate rest,
                                               EnvironmentSetting.Security.Jwks jwksConfig) {
        if (jwksUri == null || jwksUri.isBlank()) {
            if (jwksConfig.isFailStartupOnUnreachable()) {
                throw new IllegalStateException("JWKS URI is blank; cannot validate reachability.");
            }
            return;
        }
        if (!jwksUri.startsWith("https://")) {
            if (jwksConfig.isFailStartupOnUnreachable()) {
                throw new IllegalStateException("JWKS URI must use HTTPS: " + jwksUri);
            }
            log.warn("JWKS URI does not use HTTPS; skipping reachability check.");
            return;
        }
        try {
            rest.getForObject(jwksUri, String.class);
        } catch (Exception e) {
            if (jwksConfig.isFailStartupOnUnreachable()) {
                throw new IllegalStateException(
                        "JWKS URI not reachable at startup: " + jwksUri + ". " + e.getMessage(), e);
            }
            log.warn("JWKS URI not reachable at startup ({}). Continuing; failures may occur at runtime. {}",
                    jwksUri, e.getMessage());
        }
    }

    private static Set<String> parseWellKnownTenants(String csv) {
        if (csv == null || csv.isBlank()) {
            return Set.of("common", "organizations", "consumers");
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    private static OAuth2TokenValidator<Jwt> microsoftTokenValidator(String audience,
                                                                     String authorityHost,
                                                                     Set<String> wellKnownTenants) {
        OAuth2TokenValidator<Jwt> defaultValidator = JwtValidators.createDefault();
        return jwt -> {
            OAuth2TokenValidatorResult defaultResult = defaultValidator.validate(jwt);
            if (defaultResult.hasErrors()) {
                return defaultResult;
            }
            List<String> audiences = jwt.getAudience() != null ? jwt.getAudience() : List.of();
            if (!audiences.contains(audience)) {
                return OAuth2TokenValidatorResult.failure(
                        new OAuth2Error(OAuth2ErrorCodes.INVALID_REQUEST, "Invalid audience", null));
            }
            String iss = jwt.getIssuer() != null ? jwt.getIssuer().toString() : "";
            if (!MicrosoftIssuerValidator.isValid(iss, authorityHost, wellKnownTenants)) {
                return OAuth2TokenValidatorResult.failure(
                        new OAuth2Error(OAuth2ErrorCodes.INVALID_REQUEST, "Invalid issuer", null));
            }
            Map<String, Object> headers = jwt.getHeaders();
            Object typ = headers != null ? headers.get("typ") : null;
            if (typ != null && HEADER_TYP_ACCESS_TOKEN.equalsIgnoreCase(typ.toString())) {
                return OAuth2TokenValidatorResult.failure(
                        new OAuth2Error(OAuth2ErrorCodes.INVALID_REQUEST, "Invalid token type", null));
            }
            Object alg = headers != null ? headers.get("alg") : null;
            if (alg != null && HEADER_ALG_NONE.equalsIgnoreCase(alg.toString())) {
                return OAuth2TokenValidatorResult.failure(
                        new OAuth2Error(OAuth2ErrorCodes.INVALID_REQUEST, "Invalid algorithm", null));
            }
            Object kid = headers != null ? headers.get("kid") : null;
            if (kid == null || kid.toString().isBlank()) {
                return OAuth2TokenValidatorResult.failure(
                        new OAuth2Error(OAuth2ErrorCodes.INVALID_REQUEST, "Missing kid header", null));
            }
            return OAuth2TokenValidatorResult.success();
        };
    }
}
