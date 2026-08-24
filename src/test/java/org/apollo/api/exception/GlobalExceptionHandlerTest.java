package org.apollo.api.exception;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

    @Test
    void shouldReturnConflictWhenDatabaseIntegrityIsViolated() {
        ErrorResponse response = exceptionHandler.handleDataIntegrityViolation(
                new DataIntegrityViolationException("duplicate key")
        );

        assertEquals(HttpStatus.CONFLICT.value(), response.getStatus());
        assertEquals("Operação viola uma restrição de integridade dos dados", response.getMessage());
    }
}
