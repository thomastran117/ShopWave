package backend.aspects;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

/**
 * Applies retry and circuit breaker to methods annotated with {@link OAuthResilient}.
 * Retry wraps circuit breaker so each attempt is observed by the CB (recommended).
 * Does not swallow exceptions; preserves failure for callers and global handlers.
 */
@Aspect
@Component
public class OAuthRetryAspect {

    private static final Logger log = LoggerFactory.getLogger(OAuthRetryAspect.class);

    private final RetryTemplate oauthRetryTemplate;
    private final CircuitBreaker oauthCircuitBreaker;

    public OAuthRetryAspect(
            @Qualifier("oauthRetryTemplate") RetryTemplate oauthRetryTemplate,
            @Qualifier("oauthCircuitBreaker") CircuitBreaker oauthCircuitBreaker) {
        this.oauthRetryTemplate = oauthRetryTemplate;
        this.oauthCircuitBreaker = oauthCircuitBreaker;
    }

    @Around("@annotation(backend.aspects.OAuthResilient)")
    public Object aroundOAuthVerification(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature sig = (MethodSignature) joinPoint.getSignature();
        String methodName = sig.getDeclaringType().getSimpleName() + "." + sig.getMethod().getName();

        try {
            return oauthRetryTemplate.execute((RetryCallback<Object, Throwable>) context -> {
                int attempt = context.getRetryCount() + 1;

                if (attempt > 1) {
                    log.warn("OAuth verify retry attempt {} for {}", attempt, methodName);
                }

                return oauthCircuitBreaker.executeCallable(() -> {
                    try {
                        return joinPoint.proceed();
                    } catch (Exception e) {
                        throw e;
                    } catch (Throwable t) {
                        throw new java.lang.reflect.UndeclaredThrowableException(t);
                    }
                });
            });
        } catch (CallNotPermittedException e) {
            log.warn("OAuth verify blocked (circuit open) for {}: {}", methodName, e.toString());
            throw e;
        } catch (Throwable e) {
            log.error("OAuth verify failed for {}", methodName, e);
            throw e;
        }
    }
}
