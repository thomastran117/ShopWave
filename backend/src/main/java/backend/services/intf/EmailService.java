package backend.services.intf;

import backend.dtos.responses.order.OrderResponse;

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

    /**
     * Sends a device verification email showing the browser, OS, and IP of the unrecognised
     * login attempt. The send is executed asynchronously with exponential backoff retries.
     *
     * @param toEmail  the recipient email address
     * @param token    the raw UUID device verification token
     * @param browser  browser name/version detected from the User-Agent
     * @param os       operating system detected from the User-Agent
     * @param ip       IP address of the login attempt
     */
    void sendDeviceVerificationEmail(String toEmail, String token, String browser, String os, String ip);

    /**
     * Sends an order receipt email to the customer after a successful order is placed.
     * The send is executed asynchronously with exponential backoff retries.
     *
     * @param toEmail   the customer's email address
     * @param firstName the customer's first name (used for personalised greeting; may be null)
     * @param order     the completed order response to render in the receipt
     */
    void sendOrderReceiptEmail(String toEmail, String firstName, OrderResponse order);
}
