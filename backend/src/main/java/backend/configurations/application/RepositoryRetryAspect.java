package backend.configurations.application;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class RepositoryRetryAspect {

    private static final Logger log = LoggerFactory.getLogger(RepositoryRetryAspect.class);

    private final RetryTemplate repositoryRetryTemplate;

    public RepositoryRetryAspect(RetryTemplate repositoryRetryTemplate) {
        this.repositoryRetryTemplate = repositoryRetryTemplate;
    }

    @Around("execution(* backend.repositories..*(..))")
    public Object aroundRepository(ProceedingJoinPoint joinPoint) throws Throwable {
        return repositoryRetryTemplate.execute(context -> {
            int attempt = context.getRetryCount() + 1;
            if (attempt > 1) {
                log.debug("Repository retry attempt {} for {}.{}",
                        attempt,
                        joinPoint.getSignature().getDeclaringTypeName(),
                        joinPoint.getSignature().getName());
            }
            try {
                return joinPoint.proceed();
            } catch (Throwable e) {
                if (e instanceof Error) {
                    throw (Error) e;
                }
                throw (Exception) e;
            }
        });
    }
}
