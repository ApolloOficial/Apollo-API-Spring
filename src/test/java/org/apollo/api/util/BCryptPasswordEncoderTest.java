package org.apollo.api.util;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BCryptPasswordEncoderTest {
    @Test
    void shouldMatchEncodedPassword() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encoded = encoder.encode("test-password-123");
        assertTrue(encoder.matches("test-password-123", encoded));
    }
}
