package com.mentor.shared.security;

import com.mentor.shared.exception.ErrorConstantException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;

public class JsonErrorWriter {

    private JsonErrorWriter() {
    }

    static void write(HttpServletResponse response, HttpStatus status,
                      ErrorConstantException code, String type) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(String.format(
                "{\"status\":%d,\"message\":\"%s\",\"data\":null,"
                        + "\"error\":{\"type\":\"%s\",\"code\":\"%s\",\"message\":\"%s\",\"error\":\"%s\"}}",
                status.value(), code.getMessage(), type, code.name(), code.getMessage(),
                status.getReasonPhrase()));
    }
}
