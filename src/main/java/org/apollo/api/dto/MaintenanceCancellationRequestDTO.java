package org.apollo.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MaintenanceCancellationRequestDTO(
        @NotBlank(message = "Reason is required") @Size(max = 400, message = "Reason must have at most 400 characters") String reason) {
}
