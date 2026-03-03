package backend.configurations.application;

import backend.configurations.environment.EnvironmentSetting;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

/**
 * Retry configuration for reCAPTCHA siteverify calls: exponential backoff on 5xx
 * and network errors (timeout, connection). Fail closed after retries exhausted.
 */
@Configuration
public class RecaptchaRetryConfiguration {

    @Bean("recaptchaRetryTemplate")
    @Qualifier("recaptchaRetryTemplate")
    public RetryTemplate recaptchaRetryTemplate(
            EnvironmentSetting env,
            RecaptchaRetryListener recaptchaRetryListener) {
        EnvironmentSetting.Recaptcha.Retry retry = env.getRecaptcha().getRetry();
        return RetryTemplate.builder()
                .maxAttempts(retry.getMaxAttempts())
                .exponentialBackoff(
                        retry.getInitialIntervalMs(),
                        retry.getMultiplier(),
                        retry.getMaxIntervalMs())
                .retryOn(HttpServerErrorException.class)
                .retryOn(ResourceAccessException.class)
                .traversingCauses()
                .withListener(recaptchaRetryListener)
                .build();
    }
}
