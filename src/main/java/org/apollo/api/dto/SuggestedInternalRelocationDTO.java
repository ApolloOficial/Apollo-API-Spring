package org.apollo.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apollo.api.enums.RelocationStatusEnum;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SuggestedInternalRelocationDTO {
    private Long id;

    @NotNull(message = "Lote é obrigatório")
    private UUID batchId;

    @NotNull @Positive
    private Integer quantity;

    @NotNull(message = "Unidade de destino é obrigatória")
    private UUID suggestedUnitId;

    private UUID requestedById;
    private UUID reviewedById;

    @NotBlank private String justification;
    private RelocationStatusEnum status;
}