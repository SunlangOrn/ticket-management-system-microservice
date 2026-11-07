package com.mentor.paymentservice.aba;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

/**
 * The only door to ABA. It speaks in plain values (QrCommand, boolean) so the rest of the
 * service never sees ABA's request shapes, and this package does not depend on payment code.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AbaPayWayGateway {

    private static final String PURCHASE_TYPE = "purchase";
    private static final String PAYMENT_OPTION_KHQR = "abapay_khqr";
    private static final String QR_TEMPLATE = "template3_color";
    private static final String APPROVED = "APPROVED";

    private final AbaPayWayClient client;
    private final AbaPayWayProperties properties;

    public record QrCommand(String tranId, BigDecimal amount, String currency,
                            String itemName, int quantity, BigDecimal unitPrice, long lifetimeMinutes) { }

    public record QrResult(String qrString, String qrImage, String deeplink) { }

    public QrResult createQr(QrCommand command) {
        GenerateQrResponse response = callGenerateQr(command);

        // ABA can answer HTTP 200 and still refuse in the body: no QR means no payment
        if (response == null || response.qrString() == null || response.qrString().isBlank()) {
            String code = response != null && response.status() != null ? response.status().name() : null;
            String message = response != null && response.status() != null ? response.status().name() : null;
            log.error("ABA returned no QR for tranId={} (code={}, message={})", command.tranId(), code, message);
            throw AbaPayWayException.unavailable();
        }
        return new QrResult(response.qrString(), response.qrImage(), response.abaPayDeeplink());
    }

    private GenerateQrResponse callGenerateQr(QrCommand command) {
        try {
            return client.generateQr(buildQrRequest(command));
        } catch (RestClientResponseException e) {
            log.error(
                    "ABA generate-qr answered HTTP {} for tranId={}",
                    e.getStatusCode(),
                    command.tranId()
            );
            throw AbaPayWayException.unavailable();
        } catch (ResourceAccessException e) {
            log.error(
                    "ABA generate-qr unreachable or too slow for tranId={}",
                    command.tranId(),
                    e
            );
            throw AbaPayWayException.unavailable();
        }
    }

    private GenerateQrRequest buildQrRequest(QrCommand command) {
        return new GenerateQrRequest(
                client.reqTimeNow(),                                    // req_time
                properties.merchantId(),                                // merchant_id
                command.tranId(),                                       // tran_id
                formatAmount(command.amount()),                         // amount
                encodeItems(command.itemName(), command.quantity(), command.unitPrice()), // items
                "", "", "", "",                                         // firstname, lastname, email, phone (optional)
                PURCHASE_TYPE,                                          // purchase_type
                PAYMENT_OPTION_KHQR,                                    // payment_option
                base64OrNull(properties.callbackUrl()),                 // callback_url
                command.currency(),                                     // currency
                "", "", "", "",                                         // return_deeplink, custom_fields, return_params, payout
                String.valueOf(command.lifetimeMinutes()),              // lifetime (minutes)
                QR_TEMPLATE,                                            // qr_image_template
                null);                                                  // hash: added by the client
    }

    // ---------------------------------------------------------------- ask if it was paid

    /**
     * True only when ABA says APPROVED for OUR tran id. Anything else (not scanned yet, unknown,
     * pending) means "not paid yet", never "paid". Throws 503 when ABA cannot be reached.
     */
    public boolean isPaid(String tranId) {
        try {
            CheckTransactionResponse response = client.checkTransaction(tranId);
            if (response == null) {
                log.error("ABA check-transaction returned an empty body for tranId={}", tranId);
                throw AbaPayWayException.unavailable();
            }
            log.debug("ABA check tranId={} paymentStatus={}", tranId, response.paymentStatus());
            return APPROVED.equalsIgnoreCase(response.paymentStatus());
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                log.warn("ABA check-transaction answered HTTP {} for tranId={}", e.getStatusCode(), tranId);
                throw AbaPayWayException.unavailable();
            }
            log.error("ABA check-transaction answered HTTP {} for tranId={}", e.getStatusCode(), tranId);
            throw AbaPayWayException.unavailable();
        } catch (ResourceAccessException e) {
            log.error("ABA check-transaction unreachable or too slow for tranId={}", tranId, e);
            throw AbaPayWayException.unavailable();
        }
    }

    // ---------------------------------------------------------------- formatting helpers

    /** USD has 2 decimals. KHR has none: switch to scale 0 if you sell in riel. */
    private static String formatAmount(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    /** Base64 of a JSON array: [{"name":..,"quantity":..,"price":..}]. Package-private for tests. */
    static String encodeItems(String itemName, int quantity, BigDecimal unitPrice) {
        String json = "[{\"name\":" + quote(itemName)
                + ",\"quantity\":" + quantity
                + ",\"price\":" + formatAmount(unitPrice) + "}]";
        return Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

    // The ticket name is typed by an organizer: it must be escaped, or it could break the JSON
    private static String quote(String value) {
        StringBuilder out = new StringBuilder("\"");
        for (char c : value.toCharArray()) {
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
                }
            }
        }
        return out.append('"').toString();
    }

    private static String base64OrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }
}
