package backend.services.intf;

/**
 * Abstraction for CAPTCHA verification (e.g. Google reCAPTCHA v2).
 * Implementations verify the client-provided response token with the provider.
 */
public interface CaptchaService {

    /**
     * Verify a CAPTCHA response token from the client.
     *
     * @param responseToken the token from the client (e.g. g-recaptcha-response)
     * @return true if verification succeeded, false otherwise
     */
    boolean verify(String responseToken);

    /**
     * Verify a CAPTCHA response token with optional client IP for provider logging/analytics.
     *
     * @param responseToken the token from the client
     * @param remoteIp      optional client IP (may be null)
     * @return true if verification succeeded, false otherwise
     */
    default boolean verify(String responseToken, String remoteIp) {
        return verify(responseToken);
    }
}
