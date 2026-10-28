package com.mentor.userservice.dto;

import com.mentor.userservice.entity.Role;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String fullName,
        Role role) { }
