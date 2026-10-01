package org.apollo.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record MaintenanceCompleteDTO(
        @NotBlank(message = "Technical report is required") String technicalReport,
        @DecimalMin(value = "0.0", message = "Labor cost cannot be negative") BigDecimal laborCost) {
}
