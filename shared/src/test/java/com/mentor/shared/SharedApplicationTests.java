package com.mentor.shared;

import com.mentor.shared.entity.HttpBodyResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SharedApplicationTests {
    @Test
    void responseUsesDefaultSuccessMessage() {
        HttpBodyResponse<Object> response = HttpBodyResponse.builder().build();

        assertEquals(HttpBodyResponse.DEFAULT_MESSAGE, response.getMessage());
    }
}
