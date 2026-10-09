package org.apollo.api.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PhoneMaskTest {

    @Test
    void shouldKeepOnlyDigits() {
        assertEquals("85987654321", PhoneMask.digitsOnly("(85) 98765-4321"));
        assertEquals("", PhoneMask.digitsOnly(null));
    }

    @Test
    void shouldMaskMobileNumber() {
        assertEquals("(85) 9****-4321", PhoneMask.mask("85987654321"));
    }

    @Test
    void shouldMaskLandlineNumber() {
        assertEquals("(85) ****-4321", PhoneMask.mask("8532144321"));
    }

    @Test
    void shouldReturnNullWhenThereIsNoValidPhone() {
        assertNull(PhoneMask.mask(null));
        assertNull(PhoneMask.mask("123"));
    }

    @Test
    void shouldValidateBrazilianNumbers() {
        assertTrue(PhoneMask.isValid("85987654321"));
        assertTrue(PhoneMask.isValid("8532144321"));
        assertFalse(PhoneMask.isValid("85887654321"));
        assertFalse(PhoneMask.isValid("05987654321"));
        assertFalse(PhoneMask.isValid("859876543"));
        assertFalse(PhoneMask.isValid(null));
    }
}
