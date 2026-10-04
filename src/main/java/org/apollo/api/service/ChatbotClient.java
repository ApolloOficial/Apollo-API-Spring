package org.apollo.api.service;

import java.util.Map;
import org.apollo.api.dto.ChatbotRequestDTO;
import org.apollo.api.dto.ChatbotResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

@Component
public class ChatbotClient {

    private static final Logger log = LoggerFactory.getLogger(ChatbotClient.class);
    private static final String USER_HEADER = "X-User-ID";
    private static final String UNAVAILABLE_MESSAGE = "Chatbot is temporarily unavailable. Try again in a moment.";

    private final RestClient restClient;
    private final String token;

    public ChatbotClient(@Qualifier("chatbotRestClient") RestClient restClient,
                         @Value("${apollo.chatbot.token:}") String token) {
        this.restClient = restClient;
        this.token = token;
    }

    public ChatbotResponseDTO chat(String userId, ChatbotRequestDTO request) {
        requireToken();
        try {
            return restClient.post()
                    .uri("/chat")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .header(USER_HEADER, userId)
                    .body(request)
                    .exchange((req, res) -> {
                        int status = res.getStatusCode().value();
                        if (status == HttpStatus.OK.value()) {
                            ChatbotResponseDTO body = res.bodyTo(ChatbotResponseDTO.class);
                            if (body == null) {
                                throw unavailable();
                            }
                            return body;
                        }
                        throw mapFailure(status);
                    });
        } catch (RestClientException exception) {
            log.warn("Chatbot call failed: {}", exception.getMessage());
            throw unavailable();
        }
    }

    public void closeSession(String userId, String sessionId) {
        requireToken();
        try {
            restClient.post()
                    .uri("/sessions/{sessionId}/close", sessionId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .header(USER_HEADER, userId)
                    .body(Map.of("user_id", userId))
                    .exchange((req, res) -> {
                        int status = res.getStatusCode().value();
                        if (status != HttpStatus.OK.value()) {
                            throw mapFailure(status);
                        }
                        return null;
                    });
        } catch (RestClientException exception) {
            log.warn("Chatbot close call failed: {}", exception.getMessage());
            throw unavailable();
        }
    }

    private void requireToken() {
        if (token == null || token.isBlank()) {
            log.error("apollo.chatbot.token is not configured");
            throw unavailable();
        }
    }

    private ResponseStatusException mapFailure(int status) {
        log.warn("Chatbot answered with HTTP {}", status);
        if (status == HttpStatus.FORBIDDEN.value()) {
            return new ResponseStatusException(HttpStatus.FORBIDDEN, "Chat session does not belong to this user");
        }
        if (status == HttpStatus.UNPROCESSABLE_ENTITY.value()) {
            return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid chat request");
        }
        if (status == HttpStatus.UNAUTHORIZED.value()) {
            return new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Chatbot rejected the service credentials");
        }
        return unavailable();
    }

    private ResponseStatusException unavailable() {
        return new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, UNAVAILABLE_MESSAGE);
    }
}
