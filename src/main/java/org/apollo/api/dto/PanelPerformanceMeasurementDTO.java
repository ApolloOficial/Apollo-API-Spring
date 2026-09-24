package org.apollo.api.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apollo.api.enums.DataSourceEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PanelPerformanceMeasurementDTO {
    private Long id;

    @NotNull(message = "Painel é obrigatório")
    private Long panelId;

    private Long climaticIndexId;

    @NotNull
    private LocalDateTime measuredAt;

    @NotNull @DecimalMin("0.0")
    private BigDecimal generatedEnergyKwh;

    @NotNull @DecimalMin("0.0") @DecimalMax("100.0")
    private BigDecimal currentEfficiency;

    @NotNull @DecimalMin("0.0") @DecimalMax("100.0")
    private BigDecimal healthScore;

    @NotNull
    private DataSourceEnum dataSource;
}