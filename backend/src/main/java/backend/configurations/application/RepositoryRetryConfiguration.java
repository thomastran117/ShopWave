package backend.configurations.application;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.retry.support.RetryTemplate;

import java.sql.SQLTransientException;

/**
 * Retry configuration for repository layer: exponential backoff and retries
 * on transient data access errors (e.g. connection loss, deadlock, timeout).
 * Config values are clamped to safe ranges to avoid extreme settings from env.
 */
@Configuration
public class RepositoryRetryConfiguration {

    private static final int DEFAULT_MAX_ATTEMPTS = 4;
    private static final long DEFAULT_INITIAL_INTERVAL_MS = 100;
    private static final double DEFAULT_MULTIPLIER = 2.0;
    private static final long DEFAULT_MAX_INTERVAL_MS = 10_000;

    private static final int MIN_ATTEMPTS = 1;
    private static final int MAX_ATTEMPTS = 10;
    private static final long MIN_INITIAL_MS = 10;
    private static final long MAX_INITIAL_MS = 60_000;
    private static final double MIN_MULTIPLIER = 1.0;
    private static final double MAX_MULTIPLIER = 5.0;
    private static final long MIN_MAX_INTERVAL_MS = 1_000;
    private static final long MAX_MAX_INTERVAL_MS = 300_000;

    @Bean
    public RetryTemplate repositoryRetryTemplate(
            @Value("${app.repository.retry.max-attempts:" + DEFAULT_MAX_ATTEMPTS + "}") int maxAttempts,
            @Value("${app.repository.retry.initial-interval-ms:" + DEFAULT_INITIAL_INTERVAL_MS + "}") long initialIntervalMs,
            @Value("${app.repository.retry.multiplier:" + DEFAULT_MULTIPLIER + "}") double multiplier,
            @Value("${app.repository.retry.max-interval-ms:" + DEFAULT_MAX_INTERVAL_MS + "}") long maxIntervalMs) {

        int attempts = clamp(maxAttempts, MIN_ATTEMPTS, MAX_ATTEMPTS);
        long initial = clamp(initialIntervalMs, MIN_INITIAL_MS, MAX_INITIAL_MS);
        double mult = clamp(multiplier, MIN_MULTIPLIER, MAX_MULTIPLIER);
        long maxInterval = clamp(maxIntervalMs, MIN_MAX_INTERVAL_MS, MAX_MAX_INTERVAL_MS);

        return RetryTemplate.builder()
                .maxAttempts(attempts)
                .exponentialBackoff(initial, mult, maxInterval)
                .retryOn(TransientDataAccessException.class)
                .retryOn(DataAccessResourceFailureException.class)
                .retryOn(SQLTransientException.class)
                .traversingCauses()
                .build();
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static long clamp(long value, long min, long max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
