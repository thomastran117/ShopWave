package backend.configurations.application;

import backend.configurations.environment.EnvironmentSetting;
import backend.security.oauth.MicrosoftIssuerValidator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * OAuth configuration layer: provides a cached Microsoft JwtDecoder with timeouts.
 * Required-field validation is centralized in {@link OAuthConfigValidator}; no duplicate checks here.
 */
@Configuration
@DependsOn("oauthConfigValidator")
public class OAuthConfiguration {

    private static final String HEADER_TYP_ACCESS_TOKEN = "at+jwt";
    private static final String HEADER_ALG_NONE = "none";

    /**
     * Single cached JwtDecoder for Microsoft ID tokens. Required-field validation is done
     * by {@link OAuthConfigValidator}; this bean assumes config is already valid.
     */
    @Bean("microsoftJwtDecoder")
    @Qualifier("microsoftJwtDecoder")
    public JwtDecoder microsoftJwtDecoder(EnvironmentSetting env,
                                          @Qualifier("oauthJwksRestTemplate") RestTemplate jwksRest) {
        if (env == null) {
            throw new IllegalStateException("EnvironmentSetting is required for microsoftJwtDecoder.");
        }
        EnvironmentSetting.Security security = env.getSecurity();
        if (security == null) {
            throw new IllegalStateException("app.security is required for microsoftJwtDecoder.");
        }
        String microsoftId = security.getMicrosoftClientId();
        String jwksUri = security.getMicrosoftJwksUri();
        if (jwksUri == null || jwksUri.isBlank()) {
            throw new IllegalStateException("Microsoft JWKS URI is not configured (app.security.microsoft-jwks-uri).");
        }
        String authorityHost = security.getMicrosoftAuthorityHost();
        Set<String> wellKnownTenants = parseWellKnownTenants(security.getMicrosoftWellKnownTenants());
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwksUri)
                .restOperations(jwksRest)
                .build();
        decoder.setJwtValidator(microsoftTokenValidator(microsoftId, authorityHost, wellKnownTenants));
        return decoder;
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
