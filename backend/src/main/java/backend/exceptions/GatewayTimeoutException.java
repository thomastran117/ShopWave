package backend.exceptions;

import org.springframework.http.HttpStatus;

public class GatewayTimeoutException extends AppHttpException {
    private static final HttpStatus DEFAULT_STATUS = HttpStatus.GATEWAY_TIMEOUT;
    private static final String DEFAULT_MESSAGE = "Gateway timeout";

    public GatewayTimeoutException() {
        super(DEFAULT_STATUS, DEFAULT_MESSAGE);
    }

    public GatewayTimeoutException(String detail) {
        super(DEFAULT_STATUS, DEFAULT_MESSAGE, null, detail);
    }

    public GatewayTimeoutException(String messageOverride, String detail) {
        super(DEFAULT_STATUS, DEFAULT_MESSAGE, messageOverride, detail);
    }
}
