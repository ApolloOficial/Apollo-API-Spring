package org.apollo.api.recovery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RecoveryCodesTest {

    @Test
    void shouldGenerateSixDigitCodes() {
        for (int i = 0; i < 200; i++) {
            assertTrue(RecoveryCodes.numericCode().matches("\\d{6}"));
        }
    }

    @Test
    void shouldGenerateDifferentUrlSafeTokens() {
        String first = RecoveryCodes.resetToken();
        String second = RecoveryCodes.resetToken();
        assertNotEquals(first, second);
        assertTrue(first.matches("[A-Za-z0-9_-]{43}"));
    }

    @Test
    void shouldHashDeterministically() {
        assertEquals(RecoveryCodes.sha256("abc"), RecoveryCodes.sha256("abc"));
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", RecoveryCodes.sha256("abc"));
    }
}
