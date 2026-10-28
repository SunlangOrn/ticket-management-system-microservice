package com.mentor.userservice.auth.service;


import java.util.Locale;
import java.util.UUID;

import com.mentor.shared.exception.HttpException;
import com.mentor.userservice.auth.dto.LoginRequest;
import com.mentor.userservice.auth.dto.RegisterRequest;
import com.mentor.userservice.auth.dto.TokenResponse;
import com.mentor.userservice.user.dto.UserResponse;
import com.mentor.userservice.user.entity.UserAccount;
import com.mentor.userservice.user.mapper.UserMapper;
import com.mentor.userservice.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final TokenService tokens;
    private final UserMapper mapper;
    // A real hash used when the email is unknown, so both failures cost the same time
    private final String dummyHash;

    public AuthService(UserRepository users, PasswordEncoder encoder, TokenService tokens, UserMapper mapper) {
        this.users = users;
        this.encoder = encoder;
        this.tokens = tokens;
        this.mapper = mapper;
        this.dummyHash = encoder.encode(UUID.randomUUID().toString());
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        try {
            // saveAndFlush so a duplicate email fails HERE; the UNIQUE constraint is the real authority
            UserAccount saved = users.saveAndFlush(UserAccount.builder()
                    .email(normalize(request.email()))
                    .passwordHash(encoder.encode(request.password()))
                    .fullName(request.fullName().trim())
                    .build());
            log.info("User registered id={}", saved.getId());
            return mapper.toResponse(saved);
        } catch (DataIntegrityViolationException e) {
            throw new HttpException(HttpStatus.CONFLICT, "Email is already registered");
        }
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        UserAccount user = users.findByEmail(normalize(request.email())).orElse(null);
        // Always run exactly one BCrypt check, so an unknown email and a wrong password look the same
        boolean passwordMatches = encoder.matches(request.password(),
                user != null ? user.getPassword() : dummyHash);
        if (user == null || !passwordMatches || !user.isEnabled()) {
            // One message for every failure: never tell the caller which part was wrong
            throw new BadCredentialsException("Invalid email or password");
        }
        log.info("User logged in id={}", user.getId());
        return tokens.issue(user);
    }

    private static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
