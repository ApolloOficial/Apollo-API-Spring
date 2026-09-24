package org.apollo.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apollo.api.enums.RelocationStatusEnum;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SuggestedExternalRelocationDTO {
    private Long id;

    @NotNull(message = "Painel é obrigatório")
    private Long panelId;

    @NotNull(message = "Empresa de destino é obrigatória")
    private Long destinationCompanyId;

    @NotNull(message = "Segmento é obrigatório")
    private Long segmentId;

    @NotBlank private String justification;
    private RelocationStatusEnum status;
}