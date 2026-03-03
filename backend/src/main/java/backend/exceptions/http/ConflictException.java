package backend.exceptions.http;

import org.springframework.http.HttpStatus;

public class ConflictException extends AppHttpException {
    private static final HttpStatus DEFAULT_STATUS = HttpStatus.CONFLICT;
    private static final String DEFAULT_MESSAGE = "Conflict";

    public ConflictException() {
        super(DEFAULT_STATUS, DEFAULT_MESSAGE);
    }

    public ConflictException(String detail) {
        super(DEFAULT_STATUS, DEFAULT_MESSAGE, null, detail);
    }

    public ConflictException(String messageOverride, String detail) {
        super(DEFAULT_STATUS, DEFAULT_MESSAGE, messageOverride, detail);
    }
}
