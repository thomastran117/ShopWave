package backend.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Set;

/**
 * Applies retries with exponential backoff to Spring Data repository read operations only.
 * Skips retry when there is an active transaction (fail-fast within transactional boundaries)
 * and for write methods (save/delete/flush) to avoid duplicate writes on transient errors.
 */
@Aspect
@Component
public class RepositoryRetryAspect {

    private static final Logger log = LoggerFactory.getLogger(RepositoryRetryAspect.class);

    private static final Set<String> WRITE_METHOD_NAMES = Set.of(
            "save", "saveAll", "saveAndFlush",
            "delete", "deleteAll", "deleteById", "deleteAllById", "deleteAllInBatch", "deleteAllByIdInBatch",
            "flush"
    );

    private final RetryTemplate repositoryRetryTemplate;

    public RepositoryRetryAspect(RetryTemplate repositoryRetryTemplate) {
        this.repositoryRetryTemplate = repositoryRetryTemplate;
    }

    @Around("execution(* org.springframework.data.repository.Repository+.*(..))")
    public Object aroundRepository(ProceedingJoinPoint joinPoint) throws Throwable {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            return joinPoint.proceed();
        }

        String methodName = ((MethodSignature) joinPoint.getSignature()).getMethod().getName();
        if (WRITE_METHOD_NAMES.contains(methodName)) {
            return joinPoint.proceed();
        }

        return repositoryRetryTemplate.execute((RetryCallback<Object, Throwable>) context -> {
            int attempt = context.getRetryCount() + 1;
            if (attempt > 1) {
                String target = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();
                if (attempt > 2) {
                    log.warn("Repository retry attempt {} for {} (transient error)", attempt, target);
                } else {
                    log.debug("Repository retry attempt {} for {}", attempt, target);
                }
            }
            return joinPoint.proceed();
        });
    }
}
