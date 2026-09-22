package org.apollo.api.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
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
        return error(HttpStatus.BAD_REQUEST, "Requisição inválida");
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ErrorResponse> forbidden(AccessDeniedException ex) {
        return error(HttpStatus.FORBIDDEN, "Acesso negado");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ErrorResponse> conflict(DataIntegrityViolationException ex) {
        return error(HttpStatus.CONFLICT, "Operação viola uma restrição de integridade dos dados");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ResponseEntity<ErrorResponse> method(HttpRequestMethodNotSupportedException ex) {
        return error(HttpStatus.METHOD_NOT_ALLOWED, "Método HTTP não permitido");
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    ResponseEntity<ErrorResponse> route(NoHandlerFoundException ex) {
        return error(HttpStatus.NOT_FOUND, "Rota não encontrada");
    }

    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<ErrorResponse> status(ResponseStatusException ex) {
        return error(HttpStatus.valueOf(ex.getStatusCode().value()), ex.getReason());
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> generic(Exception ex, HttpServletRequest request) {
        log.error("Erro não tratado em {} {}", request.getMethod(), request.getRequestURI(), ex); return error(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno do servidor");
    }

    private ResponseEntity<ErrorResponse> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ErrorResponse(status.value(), message));
    }
}
