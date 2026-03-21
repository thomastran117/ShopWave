package backend.configurations.application;

import backend.configurations.environment.EnvironmentSetting;
import backend.http.OAuthGoogleHttpTransportFactory;
import backend.security.oauth.GoogleDiscoveryIssuers;
import backend.security.oauth.MicrosoftIssuerValidator;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * OAuth bean configuration. Each provider bean is only registered when its client ID is
 * present in the environment — if the property is absent or blank the bean is skipped and
 * the provider is treated as disabled at runtime (see {@link backend.services.impl.OAuthServiceImpl}).
 */
@Configuration
@DependsOn("oauthConfigValidator")
public class OAuthConfiguration {

    private static final String HEADER_TYP_ACCESS_TOKEN = "at+jwt";
    private static final String HEADER_ALG_NONE = "none";
    private static final String APPLE_ISSUER = "https://appleid.apple.com";

    /**
     * Google ID token verifier. Only created when {@code GOOGLE_CLIENT_ID} is configured.
     * Discovery fetch runs once at startup; cached for the lifetime of the application.
     */
    @Bean("googleIdTokenVerifier")
    @Qualifier("googleIdTokenVerifier")
    @ConditionalOnExpression("T(org.springframework.util.StringUtils).hasText('${app.security.google-client-id:}')")
    public GoogleIdTokenVerifier googleIdTokenVerifier(EnvironmentSetting env) {
        String clientId = env.getSecurity().getGoogleClientId();
        var timeouts = env.getSecurity().getOauthGoogle();
        int connectMs = timeouts != null ? timeouts.getConnectTimeoutMs() : 5_000;
        int readMs = timeouts != null ? timeouts.getReadTimeoutMs() : 10_000;
        HttpTransport transport = OAuthGoogleHttpTransportFactory.build(connectMs, readMs);
        List<String> issuers = GoogleDiscoveryIssuers.getIssuers(transport);
        return new GoogleIdTokenVerifier.Builder(transport, GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(clientId))
                .setIssuers(issuers)
                .build();
    }

    /**
     * Microsoft JwtDecoder. Only created when {@code MICROSOFT_CLIENT_ID} is configured.
     */
    @Bean("microsoftJwtDecoder")
    @Qualifier("microsoftJwtDecoder")
    @ConditionalOnExpression("T(org.springframework.util.StringUtils).hasText('${app.security.microsoft-client-id:}')")
    public JwtDecoder microsoftJwtDecoder(EnvironmentSetting env,
                                          @Qualifier("oauthJwksRestTemplate") RestTemplate jwksRest) {
        EnvironmentSetting.Security security = env.getSecurity();
        String microsoftId = security.getMicrosoftClientId();
        String jwksUri = security.getMicrosoftJwksUri();
        String authorityHost = security.getMicrosoftAuthorityHost();
        Set<String> wellKnownTenants = parseWellKnownTenants(security.getMicrosoftWellKnownTenants());
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwksUri)
                .restOperations(jwksRest)
                .build();
        decoder.setJwtValidator(microsoftTokenValidator(microsoftId, authorityHost, wellKnownTenants));
        return decoder;
    }

    /**
     * Apple JwtDecoder. Only created when {@code APPLE_CLIENT_ID} is configured.
     */
    @Bean("appleJwtDecoder")
    @Qualifier("appleJwtDecoder")
    @ConditionalOnExpression("T(org.springframework.util.StringUtils).hasText('${app.security.apple-client-id:}')")
    public JwtDecoder appleJwtDecoder(EnvironmentSetting env,
                                      @Qualifier("oauthJwksRestTemplate") RestTemplate jwksRest) {
        EnvironmentSetting.Security security = env.getSecurity();
        String appleClientId = security.getAppleClientId();
        String jwksUri = security.getAppleJwksUri();
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwksUri)
                .restOperations(jwksRest)
                .build();
        decoder.setJwtValidator(appleTokenValidator(appleClientId));
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

    private static OAuth2TokenValidator<Jwt> appleTokenValidator(String audience) {
        OAuth2TokenValidator<Jwt> defaultValidator = JwtValidators.createDefault();
        return jwt -> {
            OAuth2TokenValidatorResult defaultResult = defaultValidator.validate(jwt);
            if (defaultResult.hasErrors()) {
                return defaultResult;
            }
            List<String> audiences = jwt.getAudience() != null ? jwt.getAudience() : List.of();
            if (audience != null && !audience.isBlank() && !audiences.contains(audience)) {
                return OAuth2TokenValidatorResult.failure(
                        new OAuth2Error(OAuth2ErrorCodes.INVALID_REQUEST, "Invalid audience", null));
            }
            String iss = jwt.getIssuer() != null ? jwt.getIssuer().toString() : "";
            if (!APPLE_ISSUER.equals(iss)) {
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
