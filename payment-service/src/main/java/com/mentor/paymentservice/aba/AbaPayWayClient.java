package com.mentor.paymentservice.aba;

import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Raw HTTP and signing for ABA PayWay. It knows nothing about payments or orders. */
@Slf4j
@Component
public class AbaPayWayClient {

    private static final DateTimeFormatter REQ_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final String HMAC_ALGORITHM = "HmacSHA512";

    private final AbaPayWayProperties properties;
    private final RestClient restClient;

    public AbaPayWayClient(AbaPayWayProperties properties, RestClient.Builder builder) {
        this.properties = properties;
        // A call to ABA must never be able to wait forever
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(properties.connectTimeout())
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(properties.readTimeout());
        this.restClient = builder.requestFactory(requestFactory).build();
    }

    public String reqTimeNow() {
        return OffsetDateTime.now(ZoneOffset.UTC).format(REQ_TIME_FORMAT);
    }

    public GenerateQrResponse generateQr(GenerateQrRequest requestWithoutHash) {
        String hash = sign(hashBodyOf(requestWithoutHash));
        return postJson(properties.generateQrUrl(), requestWithoutHash.withHash(hash), GenerateQrResponse.class);
    }

    public CheckTransactionResponse checkTransaction(String tranId) {
        String reqTime = reqTimeNow();
        String hash = sign(reqTime + properties.merchantId() + tranId);
        log.debug("ABA check-transaction request: tranId={}", tranId); // never log the hash
        return postJson(properties.checkTransactionUrl(),
                new CheckTransactionRequest(reqTime, properties.merchantId(), tranId, hash),
                CheckTransactionResponse.class);
    }

    private <T> T postJson(String url, Object body, Class<T> responseType) {
        return restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(responseType);
    }

    /** The order of these fields is defined by ABA: do not reorder. */
    private String hashBodyOf(GenerateQrRequest r) {
        return orEmpty(r.reqTime())
                + orEmpty(r.merchantId())
                + orEmpty(r.tranId())
                + orEmpty(r.amount())
                + orEmpty(r.items())
                + orEmpty(r.firstname())
                + orEmpty(r.lastname())
                + orEmpty(r.email())
                + orEmpty(r.phone())
                + orEmpty(r.purchaseType())
                + orEmpty(r.paymentOption())
                + orEmpty(r.callbackUrl())
                + orEmpty(r.currency())
                + orEmpty(r.returnDeeplink())
                + orEmpty(r.customFields())
                + orEmpty(r.returnParams())
                + orEmpty(r.payout())
                + orEmpty(r.lifetime())
                + orEmpty(r.qrImageTemplate());
    }

    /** Base64 of HMAC-SHA512(raw, apiKey), as ABA requires. */
    private String sign(String raw) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(properties.apiKey().getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            return Base64.getEncoder().encodeToString(mac.doFinal(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Cannot sign ABA PayWay request", e);
        }
    }

    private static String orEmpty(String value) {
        return value == null ? "" : value;
    }
}