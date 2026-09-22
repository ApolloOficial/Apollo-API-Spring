package org.apollo.api.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apollo.api.enums.OperatingStatsEnum;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PanelDTO {

    private Long id;

    @NotNull(message = "Lote é obrigatório")
    private UUID batchId;

    @NotNull(message = "Ciclo de vida estimado é obrigatório")
    @Min(1) @Max(50)
    private Integer estimatedLifeCycle;

    @NotBlank @Size(max = 100)
    private String serialNumber;

    @NotBlank @Size(max = 100)
    private String barcode;

    @NotNull(message = "Status operacional é obrigatório")
    private OperatingStatsEnum operatingStats;

    @NotNull
    @DecimalMin("0.00") @DecimalMax("100.00")
    private BigDecimal ratedEfficiency;

    private LocalDate installationDt;
}