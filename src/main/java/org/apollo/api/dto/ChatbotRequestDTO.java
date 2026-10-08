package org.apollo.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ChatbotRequestDTO(@JsonProperty("user_id") String userId,
                                @JsonProperty("session_id") String sessionId,
                                String pergunta,
                                ChatbotContextDTO contexto) {
}
