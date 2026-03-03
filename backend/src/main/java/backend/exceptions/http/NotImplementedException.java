package backend.exceptions.http;

import org.springframework.http.HttpStatus;

public class NotImplementedException extends AppHttpException {
    private static final HttpStatus DEFAULT_STATUS = HttpStatus.NOT_IMPLEMENTED;
    private static final String DEFAULT_MESSAGE = "Not implemented";

    public NotImplementedException() {
        super(DEFAULT_STATUS, DEFAULT_MESSAGE);
    }

    public NotImplementedException(String detail) {
        super(DEFAULT_STATUS, DEFAULT_MESSAGE, null, detail);
    }

    public NotImplementedException(String messageOverride, String detail) {
        super(DEFAULT_STATUS, DEFAULT_MESSAGE, messageOverride, detail);
    }
}
