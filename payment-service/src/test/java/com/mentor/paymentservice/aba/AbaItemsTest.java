package com.mentor.paymentservice.aba;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.Test;

class AbaItemsTest {

    private String decode(String name, int quantity, String price) {
        String encoded = AbaPayWayGateway.encodeItems(name, quantity, new BigDecimal(price));
        return new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
    }

    @Test
    void encodesTheItemAsJson() {
        assertThat(decode("VIP", 2, "20"))
                .isEqualTo("[{\"name\":\"VIP\",\"quantity\":2,\"price\":20.00}]");
    }

    @Test
    void escapesQuotesAndBackslashes() {
        assertThat(decode("a\"b\\c", 1, "5")).contains("\"name\":\"a\\\"b\\\\c\"");
    }

    @Test
    void escapesLineBreaks() {
        assertThat(decode("line\nbreak", 1, "5"))
                .contains("line\\nbreak")
                .doesNotContain("\n");
    }
}
