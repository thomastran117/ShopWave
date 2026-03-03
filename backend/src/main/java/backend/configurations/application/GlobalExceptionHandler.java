package backend.configurations.application;

import backend.dtos.responses.general.ErrorResponse;
import backend.exceptions.http.AppHttpException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fields = new LinkedHashMap<>();
        ex.getBindingResult().getAllErrors().forEach(err -> {
            String fieldName = err instanceof FieldError fe ? fe.getField() : err.getObjectName();
            fields.put(fieldName, err.getDefaultMessage());
        });

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("message", "Validation failed");
        body.put("detail", "One or more fields are invalid.");
        body.put("errors", fields);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NoHandlerFoundException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("message", "Not found");
        body.put("detail", "No handler for " + ex.getHttpMethod() + " " + ex.getRequestURL());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        var methods = ex.getSupportedHttpMethods();
        var supported = methods == null
                ? java.util.List.<String>of()
                : methods.stream().map(HttpMethod::name).toList();
        String allowed = String.join(", ", supported);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", HttpStatus.METHOD_NOT_ALLOWED.value());
        body.put("message", "Method not allowed");
        body.put("detail", "HTTP " + ex.getMethod() + " is not allowed. Supported method(s): " + allowed);
        body.put("allowedMethods", supported);
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(body);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean unauthenticated = auth == null || !auth.isAuthenticated();
        Map<String, Object> body = new LinkedHashMap<>();
        if (unauthenticated) {
            body.put("status", HttpStatus.UNAUTHORIZED.value());
            body.put("message", "Unauthorized");
            body.put("detail", "Authentication required. Provide a valid Bearer token.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
        }
        body.put("status", HttpStatus.FORBIDDEN.value());
        body.put("message", "Forbidden");
        body.put("detail", ex.getMessage() != null ? ex.getMessage() : "You do not have permission to access this resource.");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    @ExceptionHandler(AppHttpException.class)
    public ResponseEntity<ErrorResponse> handleAppHttpError(AppHttpException ex) {
        ErrorResponse body = new ErrorResponse(
                ex.getStatus().value(),
                ex.getMessage(),
                ex.getDetail()
        );
        return ResponseEntity.status(ex.getStatus()).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnknown(Exception ex) {
        log.error("Unhandled exception", ex);

        ErrorResponse body = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal server error",
                "An unexpected error occurred."
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
