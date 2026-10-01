package org.apollo.api.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.server.ResponseStatusException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ErrorResponse> notFound(ResourceNotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(BusinessRuleException.class)
    ResponseEntity<ErrorResponse> business(BusinessRuleException ex) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream().map(e -> e.getField()+": "+e.getDefaultMessage()).collect(Collectors.joining("; ")); return error(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class, IllegalArgumentException.class})
    ResponseEntity<ErrorResponse> badRequest(Exception ex) {
        // O motivo real vai so para o log (ex.: parametro com valor invalido); a
        // resposta ao cliente continua generica.
        log.warn("Bad request: {}", ex.getMessage());
        return error(HttpStatus.BAD_REQUEST, "Invalid request");
    }

    // sort=... com campo inexistente (ex.: ["string"] ou id: ASC) nas listagens que ainda usam
    // Pageable: devolve 400 com a dica de uso em vez de 500.
    @ExceptionHandler(InvalidDataAccessApiUsageException.class)
    ResponseEntity<ErrorResponse> invalidSort(Exception ex, HttpServletRequest request) {
        String message = String.valueOf(ex.getMessage());
        if (message.contains("Sort expression") || message.contains("No property")) {
            log.warn("Invalid sort/property: {}", message);
            return error(HttpStatus.BAD_REQUEST, "Invalid sort field. Use sort=field,asc (example: sort=id,asc)");
        }
        return generic(ex, request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ErrorResponse> forbidden(AccessDeniedException ex) {
        return error(HttpStatus.FORBIDDEN, "Access denied");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ErrorResponse> conflict(DataIntegrityViolationException ex) {
        // O detalhe (nome da constraint/FK) so vai para o log do servidor; a resposta
        // ao cliente continua generica para nao expor a estrutura do banco.
        String dbMessage = businessMessageFromDb(ex);
        if (dbMessage != null) return error(HttpStatus.BAD_REQUEST, dbMessage);
        log.warn("Data integrity violation: {}", ex.getMostSpecificCause().getMessage());
        return error(HttpStatus.CONFLICT, "Operation violates a data integrity constraint");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ResponseEntity<ErrorResponse> method(HttpRequestMethodNotSupportedException ex) {
        return error(HttpStatus.METHOD_NOT_ALLOWED, "HTTP method not allowed");
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    ResponseEntity<ErrorResponse> route(NoHandlerFoundException ex) {
        return error(HttpStatus.NOT_FOUND, "Route not found");
    }

    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<ErrorResponse> status(ResponseStatusException ex) {
        return error(HttpStatus.valueOf(ex.getStatusCode().value()), ex.getReason());
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> generic(Exception ex, HttpServletRequest request) {
        // Regras de negocio do banco (RAISE EXCEPTION em trigger/procedure => SQLSTATE P0001) viram 400.
        String dbMessage = businessMessageFromDb(ex);
        if (dbMessage != null) {
            log.warn("Business rule from database: {}", dbMessage);
            return error(HttpStatus.BAD_REQUEST, dbMessage);
        }
        log.error("Unhandled error in {} {}", request.getMethod(), request.getRequestURI(), ex); return error(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
    }

    private static String businessMessageFromDb(Throwable ex) {
        for (Throwable t = ex; t != null; t = t.getCause()) {
            if (t instanceof java.sql.SQLException sql && "P0001".equals(sql.getSQLState())) {
                String message = String.valueOf(sql.getMessage());
                int nl = message.indexOf('\n');
                if (nl >= 0) message = message.substring(0, nl);
                message = message.replaceFirst("^(ERROR|ERRO):\\s*", "").trim();
                return message.isEmpty() ? "Business rule violated" : message;
            }
            if (t.getCause() == t) break;
        }
        return null;
    }

    private ResponseEntity<ErrorResponse> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ErrorResponse(status.value(), message));
    }
}
