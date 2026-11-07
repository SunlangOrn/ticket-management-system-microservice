package com.mentor.paymentservice.payment;

import java.security.SecureRandom;
import org.springframework.stereotype.Component;

/** Builds the id we give ABA: 16 characters, random, hard to guess. */
@Component
public class TranIdGenerator {

    // 32 symbols, without 0/O and 1/I, which are easy to misread
    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int RANDOM_LENGTH = 15;

    private final SecureRandom random = new SecureRandom();

    public String next() {
        StringBuilder id = new StringBuilder("T");
        for (int i = 0; i < RANDOM_LENGTH; i++) {
            id.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return id.toString(); // 32^15 = 2^75 possibilities; the UNIQUE index catches a collision
    }
}