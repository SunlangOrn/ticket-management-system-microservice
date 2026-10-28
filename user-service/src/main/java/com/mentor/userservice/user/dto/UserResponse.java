package com.mentor.userservice.user.dto;

import com.mentor.userservice.user.entity.Role;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String fullName,
        Role role) { }
