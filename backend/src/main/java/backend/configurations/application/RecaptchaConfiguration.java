package backend.configurations.application;

import backend.configurations.environment.EnvironmentSetting;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * reCAPTCHA HTTP client: RestTemplate bean with timeouts for siteverify API calls.
 * Defining as a bean improves testability (e.g. mock or replace in tests).
 */
@Configuration
public class RecaptchaConfiguration {

    @Bean(name = "recaptchaRestTemplate")
    public RestTemplate recaptchaRestTemplate(EnvironmentSetting env) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(java.time.Duration.ofMillis(env.getRecaptcha().getConnectTimeoutMs()));
        factory.setReadTimeout(java.time.Duration.ofMillis(env.getRecaptcha().getReadTimeoutMs()));
        return new RestTemplate(factory);
    }
}
