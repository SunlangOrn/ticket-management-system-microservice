package com.mentor.shared.security;

import com.mentor.shared.exception.ErrorConstantException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

public class JsonAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException exception) throws IOException {
        JsonErrorWriter.write(response, HttpStatus.FORBIDDEN,
                ErrorConstantException.FORBIDDEN, "AccessDeniedException");
    }
}
