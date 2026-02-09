package backend.exceptions;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends AppHttpException {
    private static final HttpStatus DEFAULT_STATUS = HttpStatus.UNAUTHORIZED;
    private static final String DEFAULT_MESSAGE = "Unauthorized";

    public UnauthorizedException() {
        super(DEFAULT_STATUS, DEFAULT_MESSAGE);
    }

    public UnauthorizedException(String detail) {
        super(DEFAULT_STATUS, DEFAULT_MESSAGE, null, detail);
    }

    public UnauthorizedException(String messageOverride, String detail) {
        super(DEFAULT_STATUS, DEFAULT_MESSAGE, messageOverride, detail);
    }
}
