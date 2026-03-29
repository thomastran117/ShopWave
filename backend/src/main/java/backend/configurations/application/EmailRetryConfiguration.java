package backend.configurations.application;

import backend.configurations.environment.EnvironmentSetting;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.classify.Classifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.MailException;
import org.springframework.retry.RetryListener;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.ExceptionClassifierRetryPolicy;
import org.springframework.retry.policy.NeverRetryPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import jakarta.mail.MessagingException;

@Configuration
public class EmailRetryConfiguration {

    private final EnvironmentSetting env;
    private final EmailRetryListener emailRetryListener;

    public EmailRetryConfiguration(EnvironmentSetting env, EmailRetryListener emailRetryListener) {
        this.env = env;
        this.emailRetryListener = emailRetryListener;
    }

    @Bean("emailRetryTemplate")
    @Qualifier("emailRetryTemplate")
    public RetryTemplate emailRetryTemplate() {
        EnvironmentSetting.Email.Retry retry = env.getEmail().getRetry();

        ExponentialBackOffPolicy backOff = new ExponentialBackOffPolicy();
        backOff.setInitialInterval(retry.getInitialIntervalMs());
        backOff.setMultiplier(retry.getMultiplier());
        backOff.setMaxInterval(retry.getMaxIntervalMs());

        ExceptionClassifierRetryPolicy retryPolicy = new ExceptionClassifierRetryPolicy();
        retryPolicy.setExceptionClassifier((Classifier<Throwable, RetryPolicy>) t -> {
            if (isTransientEmailError(t)) {
                return new SimpleRetryPolicy(retry.getMaxAttempts());
            }
            return new NeverRetryPolicy();
        });

        RetryTemplate template = new RetryTemplate();
        template.setBackOffPolicy(backOff);
        template.setRetryPolicy(retryPolicy);
        template.setListeners(new RetryListener[]{ emailRetryListener });
        return template;
    }

    private static boolean isTransientEmailError(Throwable t) {
        Throwable current = t;
        while (current != null) {
            if (current instanceof MailException) return true;
            if (current instanceof MessagingException) return true;
            current = current.getCause();
        }
        return false;
    }
}
