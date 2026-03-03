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
 */
@Configuration
public class RepositoryRetryConfiguration {

    private static final int DEFAULT_MAX_ATTEMPTS = 4;
    private static final long DEFAULT_INITIAL_INTERVAL_MS = 100;
    private static final double DEFAULT_MULTIPLIER = 2.0;
    private static final long DEFAULT_MAX_INTERVAL_MS = 10_000;

    @Bean
    public RetryTemplate repositoryRetryTemplate(
            @Value("${app.repository.retry.max-attempts:" + DEFAULT_MAX_ATTEMPTS + "}") int maxAttempts,
            @Value("${app.repository.retry.initial-interval-ms:" + DEFAULT_INITIAL_INTERVAL_MS + "}") long initialIntervalMs,
            @Value("${app.repository.retry.multiplier:" + DEFAULT_MULTIPLIER + "}") double multiplier,
            @Value("${app.repository.retry.max-interval-ms:" + DEFAULT_MAX_INTERVAL_MS + "}") long maxIntervalMs) {

        return RetryTemplate.builder()
                .maxAttempts(maxAttempts)
                .exponentialBackoff(initialIntervalMs, multiplier, maxIntervalMs)
                .retryOn(TransientDataAccessException.class)
                .retryOn(DataAccessResourceFailureException.class)
                .retryOn(SQLTransientException.class)
                .traversingCauses()
                .build();
    }
}
