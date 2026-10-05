package org.apollo.api.recovery;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class EmailMaskTest {

    @Test
    void shouldKeepOnlyFirstCharacterAndDomain() {
        assertEquals("e***@empresa.com", EmailMask.mask("enzo.mota@empresa.com"));
    }

    @Test
    void shouldTrimBeforeMasking() {
        assertEquals("a***@x.io", EmailMask.mask("  ana@x.io "));
    }

    @Test
    void shouldHideInvalidAddresses() {
        assertEquals("***", EmailMask.mask("sem-arroba"));
        assertEquals("***", EmailMask.mask("@dominio.com"));
        assertEquals("", EmailMask.mask(null));
    }
}
