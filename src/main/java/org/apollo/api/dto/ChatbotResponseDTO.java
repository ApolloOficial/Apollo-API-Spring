package org.apollo.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ChatbotResponseDTO(@JsonProperty("session_id") String sessionId,
                                 String resposta,
                                 String status,
                                 String rota,
                                 @JsonProperty("agentes_chamados") List<String> agentesChamados,
                                 List<ChatbotSourceDTO> fontes,
                                 @JsonProperty("alerta_seguranca") String alertaSeguranca,
                                 @JsonProperty("motivo_bloqueio") String motivoBloqueio) {
}
