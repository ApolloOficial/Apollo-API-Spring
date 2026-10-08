package org.apollo.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.apollo.api.enums.PriorityEnum;
import org.apollo.api.enums.WarningTypeEnum;

// Relato de campo do tecnico: tipos aceitos para placa = DANO_FISICO, SUJIDADE, PONTO_QUENTE, CONEXAO.
public record PanelIssueDTO(
        @NotBlank(message = "Barcode is required") String barcode,
        @NotNull(message = "Type is required") WarningTypeEnum type,
        @NotNull(message = "Severity is required") PriorityEnum severity,
        @NotBlank(message = "Message is required") String message) {
}
