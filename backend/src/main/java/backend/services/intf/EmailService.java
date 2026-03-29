package backend.services.intf;

public interface EmailService {

    /**
     * Sends a verification email to the given address containing a link with the token.
     * The send is executed asynchronously on a dedicated thread pool with exponential
     * backoff retries on transient SMTP failures.
     *
     * @param toEmail the recipient email address
     * @param token   the raw UUID verification token
     */
    void sendVerificationEmail(String toEmail, String token);
}
