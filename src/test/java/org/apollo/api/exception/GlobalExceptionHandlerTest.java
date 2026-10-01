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
        assertEquals("Operation violates a data integrity constraint", response.getBody().getMessage());
    }

    @Test
    void shouldPreserveResponseStatusExceptions() {
        ResponseEntity<ErrorResponse> response = exceptionHandler.status(
                new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials")
        );

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getBody().getStatus());
        assertEquals("Invalid credentials", response.getBody().getMessage());
    }

    // Regras de negocio do banco (RAISE EXCEPTION em trigger/procedure => SQLSTATE P0001) viram 400
    // com a primeira linha da mensagem, sem o prefixo "ERROR:".
    @Test
    void shouldReturnBadRequestWhenDatabaseRaisesBusinessRule() {
        java.sql.SQLException sql = new java.sql.SQLException(
                "ERROR: Panel is not in stock\n  Where: PL/pgSQL function pr_activate_panel", "P0001");

        ResponseEntity<ErrorResponse> response = exceptionHandler.conflict(
                new DataIntegrityViolationException("could not execute statement", sql));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Panel is not in stock", response.getBody().getMessage());
    }
}
