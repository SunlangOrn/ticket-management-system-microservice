package com.mentor.paymentservice.payment;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "tms.payment")
public record PaymentProperties(
        @DefaultValue("8m") Duration window,
        @DefaultValue("1m") Duration safetyMargin,
        @DefaultValue("3m") Duration minLifetime,
        @DefaultValue("2s") Duration checkCooldown,
        @DefaultValue("USD") String currency) { }