package org.apollo.api.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PasswordRulesTest {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private int violations(String pw) {
        return validator.validate(new ChangePasswordDTO("old", pw)).size();
    }

    @Test
    void acceptsStrongPassword() {
        assertEquals(0, violations("Apollo@2026"));
    }

    @Test
    void rejectsWeakPasswords() {
        assertEquals(1, violations("apollo@2026"));   // no uppercase
        assertEquals(1, violations("APOLLO@2026"));   // no lowercase
        assertEquals(1, violations("Apollo@abcd"));   // no digit
        assertEquals(1, violations("Apollo2026x"));   // no special
        assertEquals(1, violations("Ap@1"));          // too short
    }
}
