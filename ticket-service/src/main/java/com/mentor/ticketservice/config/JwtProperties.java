package com.mentor.ticketservice.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "tms.security.jwt")
public record JwtProperties(@NotBlank String issuer, @NotBlank String jwkSetUri) { }
