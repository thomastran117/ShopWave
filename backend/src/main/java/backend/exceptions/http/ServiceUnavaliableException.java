package backend.exceptions.http;

import org.springframework.http.HttpStatus;

public class ServiceUnavaliableException extends AppHttpException {
    private static final HttpStatus DEFAULT_STATUS = HttpStatus.SERVICE_UNAVAILABLE;
    private static final String DEFAULT_MESSAGE = "Service unavaliable";

    public ServiceUnavaliableException() {
        super(DEFAULT_STATUS, DEFAULT_MESSAGE);
    }

    public ServiceUnavaliableException(String detail) {
        super(DEFAULT_STATUS, DEFAULT_MESSAGE, null, detail);
    }

    public ServiceUnavaliableException(String messageOverride, String detail) {
        super(DEFAULT_STATUS, DEFAULT_MESSAGE, messageOverride, detail);
    }
}
