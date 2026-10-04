package org.apollo.api.service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.ChatContextDTO;
import org.apollo.api.dto.ChatMessageRequestDTO;
import org.apollo.api.dto.ChatReplyDTO;
import org.apollo.api.dto.ChatSourceDTO;
import org.apollo.api.dto.ChatbotContextDTO;
import org.apollo.api.dto.ChatbotRequestDTO;
import org.apollo.api.dto.ChatbotResponseDTO;
import org.apollo.api.exception.BusinessRuleException;
import org.apollo.api.security.TenantContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ChatGatewayService {

    static final int MAX_MESSAGES_PER_WINDOW = 15;
    static final Duration WINDOW = Duration.ofMinutes(1);
    private static final Pattern SESSION_ID = Pattern.compile("^[A-Za-z0-9_.:-]{8,128}$");

    private final ConcurrentHashMap<UUID, Deque<Instant>> recentMessages = new ConcurrentHashMap<>();

    private final TenantContext tenantContext;
    private final ChatbotClient chatbotClient;

    public ChatReplyDTO send(ChatMessageRequestDTO dto) {
        UUID employeeId = tenantContext.getUserId();
        enforceRateLimit(employeeId);
        String userId = chatUserId(employeeId);
        String sessionId = dto.getSessionId() == null || dto.getSessionId().isBlank()
                ? UUID.randomUUID().toString()
                : dto.getSessionId();
        ChatbotRequestDTO request = new ChatbotRequestDTO(userId, sessionId, dto.getMessage().trim(),
                toChatbotContext(dto.getContext()));
        return toReply(chatbotClient.chat(userId, request), sessionId);
    }

    public void close(String sessionId) {
        if (sessionId == null || !SESSION_ID.matcher(sessionId).matches()) {
            throw new BusinessRuleException("Invalid session id");
        }
        chatbotClient.closeSession(chatUserId(tenantContext.getUserId()), sessionId);
    }

    static String chatUserId(UUID employeeId) {
        return "apollo-" + employeeId;
    }

    private void enforceRateLimit(UUID employeeId) {
        Instant now = Instant.now();
        Deque<Instant> window = recentMessages.computeIfAbsent(employeeId, key -> new ArrayDeque<>());
        synchronized (window) {
            while (!window.isEmpty() && window.peekFirst().plus(WINDOW).isBefore(now)) {
                window.pollFirst();
            }
            if (window.size() >= MAX_MESSAGES_PER_WINDOW) {
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                        "Too many messages. Wait a moment and try again.");
            }
            window.addLast(now);
        }
    }

    private ChatbotContextDTO toChatbotContext(ChatContextDTO context) {
        if (context == null) {
            return null;
        }
        return new ChatbotContextDTO(blankToNull(context.getComponent()), blankToNull(context.getManufacturer()),
                blankToNull(context.getModel()), blankToNull(context.getSerial()),
                blankToNull(context.getBarcode()), blankToNull(context.getSymptom()),
                blankToNull(context.getAlert()), blankToNull(context.getMeasurements()),
                blankToNull(context.getEnvironmentalCondition()), blankToNull(context.getPerformedProcedure()));
    }

    private ChatReplyDTO toReply(ChatbotResponseDTO response, String requestedSessionId) {
        List<ChatSourceDTO> sources = response.fontes() == null ? List.of() : response.fontes().stream()
                .map(source -> new ChatSourceDTO(source.documento(), source.pagina(), source.secao(), source.url(),
                        source.trecho(), source.score()))
                .toList();
        return new ChatReplyDTO(
                response.sessionId() == null ? requestedSessionId : response.sessionId(),
                response.resposta(),
                response.status(),
                response.rota(),
                response.agentesChamados() == null ? List.of() : response.agentesChamados(),
                sources,
                response.alertaSeguranca(),
                response.motivoBloqueio());
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
