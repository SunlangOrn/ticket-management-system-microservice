package com.mentor.paymentservice.aba;

import jakarta.validation.constraints.NotBlank;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "tms.aba")
public record AbaPayWayProperties(
        @NotBlank String merchantId,
        @NotBlank String apiKey,
        @NotBlank String generateQrUrl,
        @NotBlank String checkTransactionUrl,
        String callbackUrl,
        @DefaultValue("3s") Duration connectTimeout,
        @DefaultValue("10s") Duration readTimeout) {

    // A record prints every field: never let the api key reach a log through toString()
    @Override
    public String toString() {
        return "AbaPayWayProperties[merchantId=" + merchantId + ", apiKey=***]";
    }
}