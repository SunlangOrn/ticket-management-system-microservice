package com.mentor.paymentservice.payment;

import com.mentor.shared.api.ControllerHandler;
import java.util.Map;

import com.mentor.shared.entity.HttpBodyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public endpoint (ABA has no user token). The body is NOT trusted: it only helps find the
 * payment, then we ask ABA directly. Always answers 200, so a caller learns nothing about
 * which tran ids exist.
 */
@RestController
@RequestMapping("/api/v1/payments/aba")
@RequiredArgsConstructor
public class AbaCallbackController {

    private final PaymentSettlementService settlement;

    @PostMapping("/callback")
    public ResponseEntity<HttpBodyResponse<Void>> callback(@RequestBody Map<String, Object> body) {
        if (body.get("tran_id") instanceof String tranId) {
            settlement.onAbaCallback(tranId);
        }
        return ControllerHandler.<Void>responseSucceed(null);
    }
}