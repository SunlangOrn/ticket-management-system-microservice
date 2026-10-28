package com.mentor.userservice.config;

import jakarta.validation.constraints.NotBlank;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "tms.security.jwt")
public record JwtProperties(
        @NotBlank String issuer,
        @DefaultValue("15m") Duration accessTokenTtl) { }
