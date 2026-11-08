package com.mentor.paymentservice.payment;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class TranIdGeneratorTest {

    private final TranIdGenerator generator = new TranIdGenerator();

    @Test
    void producesSixteenSafeCharacters() {
        for (int i = 0; i < 200; i++) {
            assertThat(generator.next()).matches("T[A-HJ-NP-Z2-9]{15}");
        }
    }

    @Test
    void doesNotRepeat() {
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 5_000; i++) {
            assertThat(seen.add(generator.next())).isTrue();
        }
    }
}
