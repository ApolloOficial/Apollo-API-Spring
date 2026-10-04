package org.apollo.api.dto;

import java.util.List;

public record ChatReplyDTO(String sessionId, String answer, String status, String route, List<String> agents,
                           List<ChatSourceDTO> sources, String securityAlert, String blockReason) {
}
