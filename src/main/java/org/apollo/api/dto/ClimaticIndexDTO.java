package org.apollo.api.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClimaticIndexDTO {
    private Long id;

    @NotNull(message = "Unidade é obrigatória")
    private UUID companyUnitId;

    @NotNull(message = "Data/hora da medição é obrigatória")
    private LocalDateTime measuredAt;

    @NotNull @DecimalMin("0.0")
    private BigDecimal irradiationIndex;

    @NotNull @DecimalMin("-60.0") @DecimalMax("70.0")
    private BigDecimal avgTemperature;

    @NotBlank
    private String weatherCondition;
}