package com.mentor.userservice.user.controller;

import com.mentor.shared.api.ControllerHandler;
import com.mentor.shared.entity.HttpBodyResponse;
import com.mentor.userservice.user.dto.UserResponse;
import com.mentor.userservice.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@NullMarked
public class UserController {

    private final UserService service;

    @GetMapping("/me")
    public ResponseEntity<HttpBodyResponse<UserResponse>> me(@AuthenticationPrincipal Jwt jwt) {
        return ControllerHandler.responseSucceed(service.get(UUID.fromString(Objects.requireNonNull(jwt.getSubject()))));
    }
}
