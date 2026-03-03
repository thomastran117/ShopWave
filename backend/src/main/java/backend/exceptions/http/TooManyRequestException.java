package backend.exceptions.http;

import org.springframework.http.HttpStatus;

public class TooManyRequestException extends AppHttpException {
    private static final HttpStatus DEFAULT_STATUS = HttpStatus.TOO_MANY_REQUESTS;
    private static final String DEFAULT_MESSAGE = "Too many requests";

    public TooManyRequestException() {
        super(DEFAULT_STATUS, DEFAULT_MESSAGE);
    }

    public TooManyRequestException(String detail) {
        super(DEFAULT_STATUS, DEFAULT_MESSAGE, null, detail);
    }

    public TooManyRequestException(String messageOverride, String detail) {
        super(DEFAULT_STATUS, DEFAULT_MESSAGE, messageOverride, detail);
    }
}
