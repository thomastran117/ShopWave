package backend.configurations.application;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Micrometer metrics for OAuth verification: retries and duration for visibility.
 * Circuit breaker state is available via Resilience4j metrics when actuator is enabled.
 */
@Component
@ConditionalOnBean(MeterRegistry.class)
public class OAuthMetrics {

    private static final String METRIC_PREFIX = "oauth.verification.";
    private static final String RETRIES = METRIC_PREFIX + "retries";
    private static final String DURATION = METRIC_PREFIX + "duration";

    private final Counter retryCounter;
    private final Timer verificationTimer;

    public OAuthMetrics(MeterRegistry registry) {
        this.retryCounter = Counter.builder(RETRIES)
                .description("Number of OAuth verification retries after transient failure")
                .register(registry);
        this.verificationTimer = Timer.builder(DURATION)
                .description("OAuth verification call duration")
                .register(registry);
    }

    public void recordRetry() {
        retryCounter.increment();
    }

    public void recordDuration(long durationMs) {
        verificationTimer.record(durationMs, TimeUnit.MILLISECONDS);
    }

    public Timer getVerificationTimer() {
        return verificationTimer;
    }
}
