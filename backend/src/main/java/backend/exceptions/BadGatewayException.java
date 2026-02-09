package backend.exceptions;

import org.springframework.http.HttpStatus;

public class BadGatewayException extends AppHttpException {
    private static final HttpStatus DEFAULT_STATUS = HttpStatus.NOT_FOUND;
    private static final String DEFAULT_MESSAGE = "Bad gateway exception";

    public BadGatewayException() {
        super(DEFAULT_STATUS, DEFAULT_MESSAGE);
    }

    public BadGatewayException(String detail) {
        super(DEFAULT_STATUS, DEFAULT_MESSAGE, null, detail);
    }

    public BadGatewayException(String messageOverride, String detail) {
        super(DEFAULT_STATUS, DEFAULT_MESSAGE, messageOverride, detail);
    }
}
