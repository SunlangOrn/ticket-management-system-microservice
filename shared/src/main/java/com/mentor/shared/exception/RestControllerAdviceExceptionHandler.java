package com.mentor.shared.exception;

import com.mentor.shared.entity.HttpBodyErrorResponse;
import com.mentor.shared.entity.HttpBodyResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class RestControllerAdviceExceptionHandler {

    @ExceptionHandler(HttpException.class)
    public ResponseEntity<@NonNull HttpBodyResponse<Object>> handleHttpException(HttpException ex) {
        if (ex.getHttpStatus().is5xxServerError()) {
            log.error("HttpException {}", ex.getHttpStatus(), ex);
        } else {
            log.debug("HttpException {}: {}", ex.getHttpStatus(), ex.getMessage());
        }
        ErrorConstantException code = ex.getError() != null ? ex.getError() : ErrorConstantException.ERROR;
        return buildResponse(ex.getHttpStatus(), ex.getClass().getSimpleName(), code, ex.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<@NonNull HttpBodyResponse<Object>> handleInvalidBody(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(fe -> errors.putIfAbsent(fe.getField(), fe.getDefaultMessage()));
        ex.getBindingResult().getGlobalErrors()
                .forEach(ge -> errors.putIfAbsent(ge.getObjectName(), ge.getDefaultMessage()));
        return validationResponse(ex, errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<@NonNull HttpBodyResponse<Object>> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getConstraintViolations()
                .forEach(v -> errors.putIfAbsent(v.getPropertyPath().toString(), v.getMessage()));
        return validationResponse(ex, errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<@NonNull HttpBodyResponse<Object>> handleUnreadable(HttpMessageNotReadableException ex) {
        log.debug("Unreadable request body: {}", ex.getMessage());
        ErrorConstantException code = ErrorConstantException.MALFORMED_REQUEST;
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getClass().getSimpleName(), code, code.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<@NonNull HttpBodyResponse<Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        ErrorConstantException code = ErrorConstantException.INVALID_PARAMETER;
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getClass().getSimpleName(), code, code.getMessage(), null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<@NonNull HttpBodyResponse<Object>> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex) {
        ErrorConstantException code = ErrorConstantException.METHOD_NOT_ALLOWED;
        return buildResponse(HttpStatus.METHOD_NOT_ALLOWED, ex.getClass().getSimpleName(), code, code.getMessage(), null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<@NonNull HttpBodyResponse<Object>> handleUnexpected(Exception ex) {
        // Spring MVC exceptions (unknown URL, unsupported media type, missing parameter) already
        // carry the right 4xx status. Without this check the catch-all would turn them into 500.
        if (ex instanceof ErrorResponse errorResponse) {
            HttpStatus status = HttpStatus.resolve(errorResponse.getStatusCode().value());
            if (status != null && status.is4xxClientError()) {
                log.debug("{}: {}", ex.getClass().getSimpleName(), ex.getMessage());
                ErrorConstantException code = codeFor(status);
                return buildResponse(status, ex.getClass().getSimpleName(), code, code.getMessage(), null);
            }
        }
        log.error("Unhandled exception", ex);
        ErrorConstantException code = ErrorConstantException.UNKNOWN_ERROR;
        // Generic message and type only: never expose exception.getMessage() or a stack trace
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "InternalServerError", code, code.getMessage(), null);
    }

    private ResponseEntity<@NonNull HttpBodyResponse<Object>> validationResponse(Exception ex, Map<String, String> errors) {
        log.debug("Validation failed: {}", errors);
        ErrorConstantException code = ErrorConstantException.VALIDATION_ERROR;
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getClass().getSimpleName(), code, code.getMessage(), errors);
    }

    static ResponseEntity<@NonNull HttpBodyResponse<Object>> buildResponse(
            HttpStatus status, String type, ErrorConstantException code, String message,  Map<String, String> errors
    ) {

        HttpBodyErrorResponse error = HttpBodyErrorResponse.builder()
                .type(type)
                .code(code.name())
                .message(message)
                .error(status.getReasonPhrase())
                .bodyRequestError(null)
                .build();

        HttpBodyResponse<Object> body = HttpBodyResponse.builder()
                .status(status.value())
                .message(message)
                .error(error)
                .build();
        return ResponseEntity.status(status).body(body);
    }

    private static ErrorConstantException codeFor(HttpStatus status) {
        return switch (status) {
            case BAD_REQUEST -> ErrorConstantException.VALIDATION_ERROR;
            case UNAUTHORIZED -> ErrorConstantException.UNAUTHORIZED;
            case FORBIDDEN -> ErrorConstantException.FORBIDDEN;
            case NOT_FOUND -> ErrorConstantException.NOT_FOUND;
            case METHOD_NOT_ALLOWED -> ErrorConstantException.METHOD_NOT_ALLOWED;
            default -> ErrorConstantException.ERROR;
        };
    }
}
