package backend.exceptions;

import org.springframework.http.HttpStatus;

public class UnsupportedMediaTypeException extends AppHttpException {
    private static final HttpStatus DEFAULT_STATUS = HttpStatus.UNSUPPORTED_MEDIA_TYPE;
    private static final String DEFAULT_MESSAGE = "Unsupported media type";

    public UnsupportedMediaTypeException() {
        super(DEFAULT_STATUS, DEFAULT_MESSAGE);
    }

    public UnsupportedMediaTypeException(String detail) {
        super(DEFAULT_STATUS, DEFAULT_MESSAGE, null, detail);
    }

    public UnsupportedMediaTypeException(String messageOverride, String detail) {
        super(DEFAULT_STATUS, DEFAULT_MESSAGE, messageOverride, detail);
    }
}
