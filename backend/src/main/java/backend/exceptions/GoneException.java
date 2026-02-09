package backend.exceptions;

import org.springframework.http.HttpStatus;

public class GoneException extends AppHttpException {
    private static final HttpStatus DEFAULT_STATUS = HttpStatus.GONE;
    private static final String DEFAULT_MESSAGE = "Gone";

    public GoneException() {
        super(DEFAULT_STATUS, DEFAULT_MESSAGE);
    }

    public GoneException(String detail) {
        super(DEFAULT_STATUS, DEFAULT_MESSAGE, null, detail);
    }

    public GoneException(String messageOverride, String detail) {
        super(DEFAULT_STATUS, DEFAULT_MESSAGE, messageOverride, detail);
    }
}
