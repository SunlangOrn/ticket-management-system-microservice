package com.mentor.userservice.exception;

import com.mentor.shared.exception.HttpException;
import org.springframework.http.HttpStatus;

public class UserException extends HttpException {
    public UserException(HttpStatus status, String message) {
        super(status,message);
    }

    public static UserException notFound() {
        return new UserException(HttpStatus.NOT_FOUND, "User not found");
    }
}
