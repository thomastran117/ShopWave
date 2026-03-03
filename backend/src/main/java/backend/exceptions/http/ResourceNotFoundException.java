package backend.exceptions.http;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends AppHttpException {
    private static final HttpStatus DEFAULT_STATUS = HttpStatus.NOT_FOUND;
    private static final String DEFAULT_MESSAGE = "Requested resoure is not found";

    public ResourceNotFoundException() {
        super(DEFAULT_STATUS, DEFAULT_MESSAGE);
    }

    public ResourceNotFoundException(String detail) {
        super(DEFAULT_STATUS, DEFAULT_MESSAGE, null, detail);
    }

    public ResourceNotFoundException(String messageOverride, String detail) {
        super(DEFAULT_STATUS, DEFAULT_MESSAGE, messageOverride, detail);
    }
}
