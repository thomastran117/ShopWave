package backend.aspects;

import java.lang.annotation.*;

/**
 * Marks a method as protected by OAuth retry and circuit breaker.
 * Used by {@link OAuthRetryAspect} for the pointcut (prefer over hardcoded method names).
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OAuthResilient {
}
