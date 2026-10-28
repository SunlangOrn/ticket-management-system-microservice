package com.mentor.userservice.auth.controller;


import com.mentor.shared.api.ControllerHandler;
import com.mentor.shared.entity.HttpBodyResponse;
import com.mentor.userservice.auth.dto.LoginRequest;
import com.mentor.userservice.auth.dto.RegisterRequest;
import com.mentor.userservice.auth.dto.TokenResponse;
import com.mentor.userservice.auth.service.AuthService;
import com.mentor.userservice.user.dto.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@NullMarked
public class AuthController {

    private final AuthService service;

    @PostMapping("/register")
    public ResponseEntity<HttpBodyResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return ControllerHandler.responseCreated(service.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<HttpBodyResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ControllerHandler.responseSucceed(service.login(request));
    }
}
