package org.apollo.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

// Abre uma OS a partir de um alerta ATIVO. Quem abre e o operador logado.
public record MaintenanceCreateDTO(
        @NotNull(message = "Warning is required") Long warningId,
        @NotNull(message = "Technician is required") UUID technicianId,
        @NotNull(message = "Maintenance type is required") Long maintenanceTypeId,
        LocalDateTime dueDate,
        @DecimalMin(value = "0.0", message = "Estimated cost cannot be negative") BigDecimal estimatedCost,
        Long parentMaintenanceId) {
}
