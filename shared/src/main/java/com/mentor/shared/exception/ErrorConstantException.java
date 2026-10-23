package com.mentor.shared.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorConstantException {

    ERROR("An error occurred"),
    UNKNOWN_ERROR("An unexpected error occurred"),
    VALIDATION_ERROR("Validation failed"),
    MALFORMED_REQUEST("Malformed request body"),
    INVALID_PARAMETER("Invalid request parameter"),
    NOT_FOUND("Resource not found"),
    UNAUTHORIZED("Authentication is required"),
    FORBIDDEN("Access is denied"),
    METHOD_NOT_ALLOWED("HTTP method not supported");

    private final String message;
}
