package backend.exceptions;

import org.springframework.http.HttpStatus;

public class InternalServerErrorException extends AppHttpException {
    private static final HttpStatus DEFAULT_STATUS = HttpStatus.INTERNAL_SERVER_ERROR;
    private static final String DEFAULT_MESSAGE = "Internal server error";

    public InternalServerErrorException() {
        super(DEFAULT_STATUS, DEFAULT_MESSAGE);
    }

    public InternalServerErrorException(String detail) {
        super(DEFAULT_STATUS, DEFAULT_MESSAGE, null, detail);
    }

    public InternalServerErrorException(String messageOverride, String detail) {
        super(DEFAULT_STATUS, DEFAULT_MESSAGE, messageOverride, detail);
    }
}
