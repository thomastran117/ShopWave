package backend.security.oauth;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.springframework.security.oauth2.jwt.JwtException;

import java.security.SignatureException;

/**
 * Single place for OAuth exception classification so the aspect and service layer
 * stay in sync and security exceptions are not misclassified. Uses explicit type
 * checks only (no class-name substring) for deterministic handling.
 */
public final class OAuthExceptionClassifier {

    private OAuthExceptionClassifier() {}

    /**
     * Returns the throwable to rethrow: either the same instance (when it must propagate
     * as-is) or a wrapped OAuthVerificationError. Fatal Errors and known OAuth types are
     * never wrapped. Use in the aspect after catching from proceed().
     */
    public static Throwable toRethrowOrWrap(Throwable t) {
        if (t == null) {
            return new OAuthVerificationError("OAuth verification failed", new IllegalStateException("null throwable"));
        }
        if (t instanceof Error) {
            return t;
        }
        if (t instanceof InvalidOAuthTokenException
                || t instanceof OAuthProviderNotConfiguredException
                || t instanceof OAuthProviderTransientException
                || t instanceof OAuthVerificationError
                || t instanceof CallNotPermittedException) {
            return t;
        }
        if (t instanceof Exception) {
            return new OAuthVerificationError("OAuth verification failed", t);
        }
        return new OAuthVerificationError("Unexpected throwable during OAuth verification", t);
    }

    /**
     * True if the throwable (or cause chain) indicates invalid token / validation failure.
     * Uses explicit exception types only; no class-name checks.
     */
    public static boolean isValidationFailure(Throwable t) {
        Throwable current = t;
        while (current != null) {
            if (current instanceof IllegalArgumentException
                    || current instanceof NumberFormatException
                    || current instanceof IllegalStateException
                    || current instanceof JwtException
                    || current instanceof SignatureException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    /**
     * Delegates to {@link OAuthRetryable#isRetryable(Throwable)} so retry/circuit-breaker
     * and service layer share the same definition.
     */
    public static boolean isRetryable(Throwable t) {
        return OAuthRetryable.isRetryable(t);
    }
}
