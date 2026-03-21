package backend.exceptions.http;

import org.springframework.http.HttpStatus;

public class PaymentException extends AppHttpException {
    private static final HttpStatus DEFAULT_STATUS = HttpStatus.PAYMENT_REQUIRED;
    private static final String DEFAULT_MESSAGE = "Payment failed";

    public PaymentException() {
        super(DEFAULT_STATUS, DEFAULT_MESSAGE);
    }

    public PaymentException(String detail) {
        super(DEFAULT_STATUS, DEFAULT_MESSAGE, null, detail);
    }

    public PaymentException(String messageOverride, String detail) {
        super(DEFAULT_STATUS, DEFAULT_MESSAGE, messageOverride, detail);
    }
}
