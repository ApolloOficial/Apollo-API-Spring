package org.apollo.api.util;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apollo.api.security.AuthenticatedUser;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

/**
 * Idempotency-Key para escritas (POST/PUT/PATCH/DELETE em /api/v1). Sem o header nada muda.
 * Com o header: a 1a chamada executa e guarda a resposta 2xx; repeticoes com a mesma chave
 * devolvem a mesma resposta sem reexecutar (Idempotent-Replay: true). Chave em andamento -> 409;
 * mesma chave em outra rota -> 422. Respostas que nao sao 2xx liberam a chave para nova tentativa.
 * Roda depois da cadeia do Spring Security, entao o usuario ja esta autenticado.
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class IdempotencyFilter extends OncePerRequestFilter {

    static final String HEADER = "Idempotency-Key";
    private static final Set<String> METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");

    private final ObjectProvider<JdbcTemplate> jdbcProvider;

    public IdempotencyFilter(ObjectProvider<JdbcTemplate> jdbcProvider) {
        this.jdbcProvider = jdbcProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String key = request.getHeader(HEADER);
        JdbcTemplate jdbc = jdbcProvider.getIfAvailable();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (key == null || jdbc == null || !METHODS.contains(request.getMethod())
                || !request.getRequestURI().startsWith("/api/v1")
                || auth == null || !(auth.getPrincipal() instanceof AuthenticatedUser user)) {
            chain.doFilter(request, response);
            return;
        }
        key = key.trim();
        if (key.isEmpty() || key.length() > 128) {
            writeError(response, 400, "BAD_REQUEST", "Idempotency-Key must have 1 to 128 characters");
            return;
        }

        String fingerprint = sha256(request.getMethod() + " " + request.getRequestURI() + "?" + request.getQueryString());
        jdbc.update("DELETE FROM idempotency_key WHERE expires_at < now()");
        int inserted = jdbc.update(
                "INSERT INTO idempotency_key (employee_id, idem_key, http_method, request_path, request_hash) "
                        + "VALUES (?, ?, ?, ?, ?) ON CONFLICT (employee_id, idem_key) DO NOTHING",
                user.getUserId(), key, request.getMethod(), request.getRequestURI(), fingerprint);

        if (inserted == 0) {
            replayOrReject(jdbc, user, key, fingerprint, response);
            return;
        }

        ContentCachingResponseWrapper wrapped = new ContentCachingResponseWrapper(response);
        try {
            chain.doFilter(request, wrapped);
        } catch (IOException | ServletException | RuntimeException exception) {
            release(jdbc, user, key);
            throw exception;
        }
        int status = wrapped.getStatus();
        if (status >= 200 && status < 300) {
            String body = new String(wrapped.getContentAsByteArray(), StandardCharsets.UTF_8);
            jdbc.update("UPDATE idempotency_key SET response_status = ?, response_body = ? "
                    + "WHERE employee_id = ? AND idem_key = ?", status, body, user.getUserId(), key);
        } else {
            release(jdbc, user, key);
        }
        wrapped.copyBodyToResponse();
    }

    private void replayOrReject(JdbcTemplate jdbc, AuthenticatedUser user, String key, String fingerprint,
                                HttpServletResponse response) throws IOException {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT request_hash, response_status, response_body FROM idempotency_key "
                        + "WHERE employee_id = ? AND idem_key = ?", user.getUserId(), key);
        if (rows.isEmpty()) {
            writeError(response, 409, "CONFLICT", "Request in progress, try again");
            return;
        }
        Map<String, Object> row = rows.getFirst();
        if (!fingerprint.equals(row.get("request_hash"))) {
            writeError(response, 422, "UNPROCESSABLE_ENTITY", "Idempotency-Key was used with a different request");
            return;
        }
        Object status = row.get("response_status");
        if (status == null) {
            writeError(response, 409, "CONFLICT", "Request in progress, try again");
            return;
        }
        response.setStatus(((Number) status).intValue());
        response.setHeader("Idempotent-Replay", "true");
        String body = (String) row.get("response_body");
        if (body != null && !body.isEmpty()) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(body);
        }
    }

    private static void release(JdbcTemplate jdbc, AuthenticatedUser user, String key) {
        jdbc.update("DELETE FROM idempotency_key WHERE employee_id = ? AND idem_key = ?", user.getUserId(), key);
    }

    private static void writeError(HttpServletResponse response, int status, String code, String message)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"status\":" + status + ",\"code\":\"" + code + "\",\"message\":\""
                + message.replace("\"", "'") + "\"}");
    }

    static String sha256(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
