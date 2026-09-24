package org.apollo.api.exception;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

    @Test
    void shouldReturnConflictWhenDatabaseIntegrityIsViolated() {
        ResponseEntity<ErrorResponse> response = exceptionHandler.conflict(
                new DataIntegrityViolationException("duplicate key")
        );

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.CONFLICT.value(), response.getBody().getStatus());
        assertEquals("Operação viola uma restrição de integridade dos dados", response.getBody().getMessage());
    }

    @Test
    void shouldPreserveResponseStatusExceptions() {
        ResponseEntity<ErrorResponse> response = exceptionHandler.status(
                new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas")
        );

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getBody().getStatus());
        assertEquals("Credenciais inválidas", response.getBody().getMessage());
    }
}
