package org.apollo.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apollo.api.enums.PriorityEnum;
import org.apollo.api.enums.WarningStatusEnum;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WarningDTO {
    private Long id;

    @NotNull(message = "Painel é obrigatório")
    private Long panelId;

    @NotBlank private String type;
    private PriorityEnum severity;
    private WarningStatusEnum status;

    @NotBlank private String message;
}