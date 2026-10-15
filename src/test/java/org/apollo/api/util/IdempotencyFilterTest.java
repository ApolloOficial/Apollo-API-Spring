package org.apollo.api.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import java.util.List;
import java.util.Map;
import org.apollo.api.security.AuthenticatedUser;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class IdempotencyFilterTest {

    private JdbcTemplate jdbc;
    private IdempotencyFilter filter;
    private FilterChain chain;

    @BeforeEach
    void setUp() {
        jdbc = mock(JdbcTemplate.class);
        @SuppressWarnings("unchecked")
        ObjectProvider<JdbcTemplate> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(jdbc);
        filter = new IdempotencyFilter(provider);
        chain = mock(FilterChain.class);
        AuthenticatedUser user = mock(AuthenticatedUser.class);
        when(user.getUserId()).thenReturn(UUID.randomUUID());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, List.of()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldPassThroughWithoutHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/stocks");

        filter.doFilter(request, new MockHttpServletResponse(), chain);

        verify(chain).doFilter(any(), any());
        verify(jdbc, never()).update(anyString(), any(Object[].class));
    }

    @Test
    void shouldReplayStoredResponseWithoutRunningAgain() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/stocks");
        request.addHeader("Idempotency-Key", "abc-123");
        String fingerprint = IdempotencyFilter.sha256("POST /api/v1/stocks?null");
        when(jdbc.update(anyString(), any(Object[].class))).thenReturn(0);
        when(jdbc.queryForList(anyString(), any(Object[].class))).thenReturn(List.of(Map.<String, Object>of(
                "request_hash", fingerprint, "response_status", 201, "response_body", "{\"id\":7}")));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        verify(chain, never()).doFilter(any(), any());
        assertEquals(201, response.getStatus());
        assertEquals("true", response.getHeader("Idempotent-Replay"));
        assertTrue(response.getContentAsString().contains("\"id\":7"));
    }

    @Test
    void shouldRejectSameKeyOnDifferentRequest() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/stocks");
        request.addHeader("Idempotency-Key", "abc-123");
        when(jdbc.update(anyString(), any(Object[].class))).thenReturn(0);
        when(jdbc.queryForList(anyString(), any(Object[].class))).thenReturn(List.of(Map.<String, Object>of(
                "request_hash", "other", "response_status", 201, "response_body", "{}")));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        assertEquals(422, response.getStatus());
    }
}
