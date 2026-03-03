package backend.exceptions.http;

import org.springframework.http.HttpStatus;

public class UnprocessableEntityException extends AppHttpException {
    private static final HttpStatus DEFAULT_STATUS = HttpStatus.valueOf(422);
    private static final String DEFAULT_MESSAGE = "Unprocessable entity";

    public UnprocessableEntityException() {
        super(DEFAULT_STATUS, DEFAULT_MESSAGE);
    }

    public UnprocessableEntityException(String detail) {
        super(DEFAULT_STATUS, DEFAULT_MESSAGE, null, detail);
    }

    public UnprocessableEntityException(String messageOverride, String detail) {
        super(DEFAULT_STATUS, DEFAULT_MESSAGE, messageOverride, detail);
    }
}
