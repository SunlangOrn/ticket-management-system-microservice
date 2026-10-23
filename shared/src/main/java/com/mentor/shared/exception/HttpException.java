package com.mentor.shared.exception;

import lombok.Getter;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;
import java.util.Objects;

@EqualsAndHashCode(callSuper = true)
@Getter
public class HttpException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final ErrorConstantException error;

    public HttpException(HttpStatus httpStatus, String message) {
        this(httpStatus, ErrorConstantException.ERROR, message);
    }

    public HttpException(HttpStatus httpStatus, ErrorConstantException error, String message) {
        super(message);
        this.httpStatus = Objects.requireNonNull(httpStatus, "httpStatus must not be null");
        this.error = error;
    }
}
