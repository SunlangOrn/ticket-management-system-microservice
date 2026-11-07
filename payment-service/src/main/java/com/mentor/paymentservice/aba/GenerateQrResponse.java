package com.mentor.paymentservice.aba;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mentor.paymentservice.payment.PaymentStatus;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GenerateQrResponse(
        PaymentStatus status,
        String qrString,
        String qrImage,
        @JsonProperty("abapay_deeplink") String abaPayDeeplink
) {}
