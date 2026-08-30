package org.apollo.api.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apollo.api.enums.OperatingStatsEnum;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PanelDTO {

    private Long id;

    @NotNull(message = "Lote é obrigatório")
    @Positive(message = "Lote deve ser válido")
    private Long batchId;

    @NotNull(message = "Unidade é obrigatória")
    @Positive(message = "Unidade deve ser válida")
    private Long coUnityId;

    @NotNull(message = "Ciclo de vida estimado é obrigatório")
    @Min(value = 1, message = "Ciclo de vida estimado deve ser de pelo menos 1 ano")
    @Max(value = 50, message = "Ciclo de vida estimado deve ser de no máximo 50 anos")
    private Integer estimatedLifeCycle;

    @NotBlank(message = "Número de série é obrigatório")
    @Size(max = 100, message = "Número de série deve ter no máximo 100 caracteres")
    private String serialNumber;

    @NotBlank(message = "Código de barras é obrigatório")
    @Size(max = 100, message = "Código de barras deve ter no máximo 100 caracteres")
    private String barcode;

    @NotNull(message = "Status operacional é obrigatório")
    private OperatingStatsEnum operatingStatsEnum;

    @NotNull(message = "Eficiência nominal é obrigatória")
    @DecimalMin(value = "0.00", message = "Eficiência nominal deve ser maior ou igual a 0")
    @DecimalMax(value = "100.00", message = "Eficiência nominal deve ser menor ou igual a 100")
    private BigDecimal ratedEfficiency;

    private LocalDate installationDt;
}
