package backend.aspects;

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
 * Applies retry with exponential backoff and circuit breaker to OAuth token
 * verification (Google and Microsoft). Does not apply to verifyAppleToken.
 * Order: circuit breaker wraps retry; when circuit is open, calls fail fast.
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

    @Around("execution(* backend.services.intf.OAuthService.verifyGoogleToken(..)) " +
            "|| execution(* backend.services.intf.OAuthService.verifyMicrosoftToken(..))")
    public Object aroundOAuthVerification(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = ((MethodSignature) joinPoint.getSignature()).getMethod().getName();
        return oauthCircuitBreaker.executeSupplier(() -> {
            try {
                return oauthRetryTemplate.execute((RetryCallback<Object, Throwable>) context -> {
                    int attempt = context.getRetryCount() + 1;
                    if (attempt > 1) {
                        log.debug("OAuth verification retry attempt {} for {}", attempt, methodName);
                    }
                    return joinPoint.proceed();
                });
            } catch (Throwable t) {
                if (t instanceof RuntimeException rt) {
                    throw rt;
                }
                throw new RuntimeException(t);
            }
        });
    }
}
