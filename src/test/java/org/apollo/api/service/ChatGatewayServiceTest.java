package org.apollo.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import org.apollo.api.dto.ChatContextDTO;
import org.apollo.api.dto.ChatMessageRequestDTO;
import org.apollo.api.dto.ChatReplyDTO;
import org.apollo.api.dto.ChatbotRequestDTO;
import org.apollo.api.dto.ChatbotResponseDTO;
import org.apollo.api.dto.ChatbotSourceDTO;
import org.apollo.api.exception.BusinessRuleException;
import org.apollo.api.security.TenantContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class ChatGatewayServiceTest {

    private static final UUID EMPLOYEE_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final String CHAT_USER = "apollo-00000000-0000-0000-0000-000000000001";

    @Mock
    private TenantContext tenantContext;

    @Mock
    private ChatbotClient chatbotClient;

    @InjectMocks
    private ChatGatewayService service;

    @Test
    void shouldUsePseudonymizedUserAndGenerateSessionWhenMissing() {
        when(tenantContext.getUserId()).thenReturn(EMPLOYEE_ID);
        when(chatbotClient.chat(eq(CHAT_USER), any(ChatbotRequestDTO.class))).thenReturn(answer(null));

        ChatReplyDTO reply = service.send(new ChatMessageRequestDTO(null, "  Panel error  ", null));

        ArgumentCaptor<ChatbotRequestDTO> captor = ArgumentCaptor.forClass(ChatbotRequestDTO.class);
        verify(chatbotClient).chat(eq(CHAT_USER), captor.capture());
        ChatbotRequestDTO sent = captor.getValue();
        assertEquals(CHAT_USER, sent.userId());
        assertEquals("Panel error", sent.pergunta());
        assertNotNull(UUID.fromString(sent.sessionId()));
        assertNull(sent.contexto());
        assertEquals(sent.sessionId(), reply.sessionId());
    }

    @Test
    void shouldKeepTheSessionAndMapContextAndSources() {
        when(tenantContext.getUserId()).thenReturn(EMPLOYEE_ID);
        when(chatbotClient.chat(eq(CHAT_USER), any(ChatbotRequestDTO.class))).thenReturn(answer("session-12345"));
        ChatContextDTO context = new ChatContextDTO("panel", null, "CS3W-410P", "CS-2026-00932", " ", "low output",
                null, null, "cloudy", null);

        ChatReplyDTO reply = service.send(new ChatMessageRequestDTO("session-12345", "Why is it low?", context));

        ArgumentCaptor<ChatbotRequestDTO> captor = ArgumentCaptor.forClass(ChatbotRequestDTO.class);
        verify(chatbotClient).chat(eq(CHAT_USER), captor.capture());
        assertEquals("session-12345", captor.getValue().sessionId());
        assertEquals("panel", captor.getValue().contexto().componente());
        assertEquals("CS3W-410P", captor.getValue().contexto().modelo());
        assertNull(captor.getValue().contexto().barcode());
        assertEquals("cloudy", captor.getValue().contexto().condicaoAmbiental());
        assertEquals("Check the string voltage.", reply.answer());
        assertEquals("sucesso", reply.status());
        assertEquals(List.of("manutencao"), reply.agents());
        assertEquals("IEA-PVPS", reply.sources().get(0).document());
        assertEquals(12, reply.sources().get(0).page());
    }

    @Test
    void shouldLimitMessagesPerMinute() {
        when(tenantContext.getUserId()).thenReturn(EMPLOYEE_ID);
        when(chatbotClient.chat(eq(CHAT_USER), any(ChatbotRequestDTO.class))).thenReturn(answer(null));
        ChatMessageRequestDTO request = new ChatMessageRequestDTO(null, "Hello", null);

        for (int i = 0; i < ChatGatewayService.MAX_MESSAGES_PER_WINDOW; i++) {
            service.send(request);
        }

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.send(request));
        assertEquals(HttpStatus.TOO_MANY_REQUESTS.value(), exception.getStatusCode().value());
    }

    @Test
    void shouldCloseSessionForTheLoggedUser() {
        when(tenantContext.getUserId()).thenReturn(EMPLOYEE_ID);

        service.close("session-12345");

        verify(chatbotClient).closeSession(CHAT_USER, "session-12345");
    }

    @Test
    void shouldRejectInvalidSessionIdOnClose() {
        assertThrows(BusinessRuleException.class, () -> service.close("short"));
        verify(chatbotClient, never()).closeSession(any(), any());
    }

    private ChatbotResponseDTO answer(String sessionId) {
        return new ChatbotResponseDTO(sessionId, "Check the string voltage.", "sucesso", "manutencao",
                List.of("manutencao"), List.of(new ChatbotSourceDTO("IEA-PVPS", 12, null, null, null, 0.8)),
                null, null);
    }
}
