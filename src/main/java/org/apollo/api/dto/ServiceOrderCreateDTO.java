package org.apollo.api.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

// Corpo de POST /api/v1/warnings/{id}/service-order. O alerta vem da URL; quem abre e o operador logado.
public record ServiceOrderCreateDTO(
        @NotNull(message = "Maintenance type is required") Long maintenanceTypeId,
        @NotNull(message = "Technician is required") UUID technicianId,
        LocalDateTime dueDate) {
}
