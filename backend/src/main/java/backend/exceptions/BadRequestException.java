package backend.exceptions;

import org.springframework.http.HttpStatus;

public class BadRequestException extends AppHttpException {
    private static final HttpStatus DEFAULT_STATUS = HttpStatus.BAD_REQUEST;
    private static final String DEFAULT_MESSAGE = "Bad request";

    public BadRequestException() {
        super(DEFAULT_STATUS, DEFAULT_MESSAGE);
    }

    public BadRequestException(String detail) {
        super(DEFAULT_STATUS, DEFAULT_MESSAGE, null, detail);
    }

    public BadRequestException(String messageOverride, String detail) {
        super(DEFAULT_STATUS, DEFAULT_MESSAGE, messageOverride, detail);
    }
}
