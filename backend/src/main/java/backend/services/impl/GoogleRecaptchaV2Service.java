package backend.services.impl;

import backend.configurations.environment.EnvironmentSetting;
import backend.services.intf.CaptchaService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/**
 * Google reCAPTCHA v2 server-side verification with retries and fail-closed behavior.
 * Verifies the client response token via the siteverify API. On 5xx or network errors,
 * retries with exponential backoff; after retries exhausted or on 4xx, returns false (fail closed).
 */
@Service
public class GoogleRecaptchaV2Service implements CaptchaService {

    private static final String SITEVERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";

    private final RestTemplate restTemplate;
    private final String secretKey;
    private final RetryTemplate recaptchaRetryTemplate;

    public GoogleRecaptchaV2Service(
            EnvironmentSetting env,
            @Qualifier("recaptchaRetryTemplate") RetryTemplate recaptchaRetryTemplate) {
        this.secretKey = env.getSecurity().getRecaptchaSecretKey();
        this.recaptchaRetryTemplate = recaptchaRetryTemplate;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(java.time.Duration.ofMillis(env.getRecaptcha().getConnectTimeoutMs()));
        factory.setReadTimeout(java.time.Duration.ofMillis(env.getRecaptcha().getReadTimeoutMs()));
        this.restTemplate = new RestTemplate(factory);
    }

    @Override
    public boolean verify(String responseToken) {
        return verify(responseToken, null);
    }

    @Override
    public boolean verify(String responseToken, String remoteIp) {
        if (responseToken == null || responseToken.isBlank()) {
            return false;
        }
        if (secretKey.isBlank()) {
            return false;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("secret", secretKey);
        body.add("response", responseToken);
        if (remoteIp != null && !remoteIp.isBlank()) {
            body.add("remoteip", remoteIp);
        }

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            return Boolean.TRUE.equals(recaptchaRetryTemplate.execute(context -> {
                try {
                    ResponseEntity<SiteVerifyResponse> response = restTemplate.postForEntity(
                            SITEVERIFY_URL,
                            request,
                            SiteVerifyResponse.class
                    );
                    SiteVerifyResponse responseBody = response.getBody();
                    return responseBody != null && Boolean.TRUE.equals(responseBody.success());
                } catch (HttpClientErrorException e) {
                    // 4xx: bad request or invalid response — fail closed, do not retry
                    return false;
                }
                // HttpServerErrorException (5xx) and ResourceAccessException (timeout, connection)
                // are not caught; they propagate so RetryTemplate retries with backoff
            }));
        } catch (Exception e) {
            // Retries exhausted (5xx or network) or any unexpected error — fail closed
            return false;
        }
    }

    /**
     * Mirrors the JSON response from Google siteverify API.
     */
    @SuppressWarnings("unused")
    private record SiteVerifyResponse(Boolean success, String challenge_ts, String hostname) {}
}
