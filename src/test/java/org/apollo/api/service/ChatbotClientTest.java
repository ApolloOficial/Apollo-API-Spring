package org.apollo.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.apollo.api.dto.ChatbotRequestDTO;
import org.apollo.api.dto.ChatbotResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

class ChatbotClientTest {

    private static final String USER_ID = "apollo-00000000-0000-0000-0000-000000000001";

    private MockRestServiceServer server;
    private ChatbotClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://chatbot.test");
        server = MockRestServiceServer.bindTo(builder).build();
        client = new ChatbotClient(builder.build(), "service-token");
    }

    @Test
    void shouldSendCredentialsAndParseTheAnswer() {
        server.expect(requestTo("http://chatbot.test/chat"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer service-token"))
                .andExpect(header("X-User-ID", USER_ID))
                .andRespond(withSuccess("""
                        {"session_id": "session-123", "resposta": "Check the string voltage.", "status": "sucesso",
                         "rota": "manutencao", "agentes_chamados": ["manutencao"],
                         "fontes": [{"documento": "IEA-PVPS", "pagina": 12}], "alerta_seguranca": null,
                         "motivo_bloqueio": null}
                        """, MediaType.APPLICATION_JSON));

        ChatbotResponseDTO response = client.chat(USER_ID, request());

        assertEquals("session-123", response.sessionId());
        assertEquals("Check the string voltage.", response.resposta());
        assertEquals("sucesso", response.status());
        assertEquals(1, response.fontes().size());
        assertEquals(12, response.fontes().get(0).pagina());
        server.verify();
    }

    @Test
    void shouldMapForbiddenToForbidden() {
        server.expect(requestTo("http://chatbot.test/chat")).andRespond(withStatus(HttpStatus.FORBIDDEN));

        assertStatus(HttpStatus.FORBIDDEN, () -> client.chat(USER_ID, request()));
    }

    @Test
    void shouldMapInvalidPayloadToBadRequest() {
        server.expect(requestTo("http://chatbot.test/chat")).andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY));

        assertStatus(HttpStatus.BAD_REQUEST, () -> client.chat(USER_ID, request()));
    }

    @Test
    void shouldMapRejectedCredentialsToBadGateway() {
        server.expect(requestTo("http://chatbot.test/chat")).andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        assertStatus(HttpStatus.BAD_GATEWAY, () -> client.chat(USER_ID, request()));
    }

    @Test
    void shouldMapServerErrorToUnavailable() {
        server.expect(requestTo("http://chatbot.test/chat")).andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        assertStatus(HttpStatus.SERVICE_UNAVAILABLE, () -> client.chat(USER_ID, request()));
    }

    @Test
    void shouldFailWithoutCallingTheServiceWhenTokenIsMissing() {
        ChatbotClient unconfigured = new ChatbotClient(RestClient.builder().baseUrl("http://chatbot.test").build(), " ");

        assertStatus(HttpStatus.SERVICE_UNAVAILABLE, () -> unconfigured.chat(USER_ID, request()));
    }

    @Test
    void shouldCloseSession() {
        server.expect(requestTo("http://chatbot.test/sessions/session-123/close"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("X-User-ID", USER_ID))
                .andRespond(withSuccess("{\"status\": \"sucesso\"}", MediaType.APPLICATION_JSON));

        client.closeSession(USER_ID, "session-123");

        server.verify();
    }

    @Test
    void shouldMapCloseForbiddenToForbidden() {
        server.expect(requestTo("http://chatbot.test/sessions/session-123/close"))
                .andRespond(withStatus(HttpStatus.FORBIDDEN));

        assertStatus(HttpStatus.FORBIDDEN, () -> client.closeSession(USER_ID, "session-123"));
    }

    private void assertStatus(HttpStatus expected, org.junit.jupiter.api.function.Executable call) {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, call);
        assertNotNull(exception.getReason());
        assertEquals(expected.value(), exception.getStatusCode().value());
    }

    private ChatbotRequestDTO request() {
        return new ChatbotRequestDTO(USER_ID, "session-123", "How do I check degradation?", null);
    }
}
