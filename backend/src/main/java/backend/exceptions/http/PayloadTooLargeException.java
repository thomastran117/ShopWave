package backend.exceptions.http;

import org.springframework.http.HttpStatus;

public class PayloadTooLargeException extends AppHttpException {
    private static final HttpStatus DEFAULT_STATUS = HttpStatus.valueOf(413);
    private static final String DEFAULT_MESSAGE = "Payload too large";

    public PayloadTooLargeException() {
        super(DEFAULT_STATUS, DEFAULT_MESSAGE);
    }

    public PayloadTooLargeException(String detail) {
        super(DEFAULT_STATUS, DEFAULT_MESSAGE, null, detail);
    }

    public PayloadTooLargeException(String messageOverride, String detail) {
        super(DEFAULT_STATUS, DEFAULT_MESSAGE, messageOverride, detail);
    }
}
