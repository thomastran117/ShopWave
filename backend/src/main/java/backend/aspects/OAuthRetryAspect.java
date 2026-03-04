package backend.aspects;

import backend.configurations.application.OAuthMetrics;
import backend.security.oauth.InvalidOAuthTokenException;
import backend.security.oauth.OAuthProviderTransientException;
import backend.security.oauth.OAuthVerificationError;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Applies retry and circuit breaker to methods annotated with {@link OAuthResilient}.
 * Retry wraps circuit breaker so each attempt is observed by the CB (recommended).
 * Integrates {@link OAuthMetrics} for duration and retry visibility when available.
 */
@Aspect
@Component
public class OAuthRetryAspect {

    private static final Logger log = LoggerFactory.getLogger(OAuthRetryAspect.class);
    private static final long RETRY_LOG_RATE_LIMIT_MS = 10_000;
    private static final AtomicLong lastRetryLogTime = new AtomicLong(0);

    private final RetryTemplate oauthRetryTemplate;
    private final CircuitBreaker oauthCircuitBreaker;
    private final OAuthMetrics oauthMetrics;

    public OAuthRetryAspect(
            @Qualifier("oauthRetryTemplate") RetryTemplate oauthRetryTemplate,
            @Qualifier("oauthCircuitBreaker") CircuitBreaker oauthCircuitBreaker,
            @Autowired(required = false) OAuthMetrics oauthMetrics) {
        this.oauthRetryTemplate = oauthRetryTemplate;
        this.oauthCircuitBreaker = oauthCircuitBreaker;
        this.oauthMetrics = oauthMetrics;
    }

    @Around("@annotation(backend.aspects.OAuthResilient)")
    public Object aroundOAuthVerification(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature sig = (MethodSignature) joinPoint.getSignature();
        String methodName = sig.getDeclaringType().getSimpleName() + "." + sig.getMethod().getName();
        long startMs = System.currentTimeMillis();

        try {
            Object result = oauthRetryTemplate.execute((RetryCallback<Object, Throwable>) context -> {
                int attempt = context.getRetryCount() + 1;

                if (attempt > 1) {
                    if (oauthMetrics != null) {
                        oauthMetrics.recordRetry();
                    }
                    long now = System.currentTimeMillis();
                    long prev = lastRetryLogTime.get();
                    if (now - prev >= RETRY_LOG_RATE_LIMIT_MS && lastRetryLogTime.compareAndSet(prev, now)) {
                        log.warn("OAuth verify retry attempt {} for {}", attempt, methodName);
                    }
                }

                return oauthCircuitBreaker.executeCallable(() -> {
                    try {
                        return joinPoint.proceed();
                    } catch (Throwable t) {
                        if (t instanceof Error e) {
                            throw e;
                        }
                        if (t instanceof Exception e) {
                            // Rethrow OAuth types as-is so retryable vs non-retryable stay distinct; no double-wrap
                            if (e instanceof InvalidOAuthTokenException
                                    || e instanceof OAuthProviderTransientException
                                    || e instanceof OAuthVerificationError) {
                                throw e;
                            }
                            throw e;
                        }
                        throw new OAuthVerificationError("Unexpected throwable during OAuth verification", t);
                    }
                });
            });
            if (oauthMetrics != null) {
                oauthMetrics.recordDuration(System.currentTimeMillis() - startMs);
            }
            return result;
        } catch (Throwable e) {
            if (oauthMetrics != null) {
                oauthMetrics.recordDuration(System.currentTimeMillis() - startMs);
            }
            // Ensure wrapped Errors always propagate without being reclassified or swallowed
            if (e instanceof Error err) {
                throw err;
            }
            if (e instanceof CallNotPermittedException cbe) {
                log.warn("OAuth verify blocked (circuit open) for {}: {}", methodName, cbe.toString());
                throw cbe;
            }
            if (e instanceof OAuthVerificationError) {
                log.error("OAuth verify failed for {} (details omitted to avoid leakage)", methodName);
            } else {
                log.error("OAuth verify failed for {}", methodName, e);
            }
            throw e;
        }
    }
}
