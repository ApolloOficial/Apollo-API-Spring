package org.apollo.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record MaintenancePartCreateDTO(
        @NotNull(message = "Part is required") Long partId,
        @NotNull(message = "Quantity is required") @Positive(message = "Quantity must be positive") Integer quantity) {
}
