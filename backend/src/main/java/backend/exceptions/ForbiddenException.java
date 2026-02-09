package backend.exceptions;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends AppHttpException {
    private static final HttpStatus DEFAULT_STATUS = HttpStatus.FORBIDDEN;
    private static final String DEFAULT_MESSAGE = "Forbidden";

    public ForbiddenException() {
        super(DEFAULT_STATUS, DEFAULT_MESSAGE);
    }

    public ForbiddenException(String detail) {
        super(DEFAULT_STATUS, DEFAULT_MESSAGE, null, detail);
    }

    public ForbiddenException(String messageOverride, String detail) {
        super(DEFAULT_STATUS, DEFAULT_MESSAGE, messageOverride, detail);
    }
}
