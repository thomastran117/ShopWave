package backend.exceptions;

import org.springframework.http.HttpStatus;

public class MethodNotAllowedException extends AppHttpException {
    private static final HttpStatus DEFAULT_STATUS = HttpStatus.METHOD_NOT_ALLOWED;
    private static final String DEFAULT_MESSAGE = "Method not allowed";

    public MethodNotAllowedException() {
        super(DEFAULT_STATUS, DEFAULT_MESSAGE);
    }

    public MethodNotAllowedException(String detail) {
        super(DEFAULT_STATUS, DEFAULT_MESSAGE, null, detail);
    }

    public MethodNotAllowedException(String messageOverride, String detail) {
        super(DEFAULT_STATUS, DEFAULT_MESSAGE, messageOverride, detail);
    }
}
