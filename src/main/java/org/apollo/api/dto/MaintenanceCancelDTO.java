package org.apollo.api.dto;

import jakarta.validation.constraints.NotBlank;

public record MaintenanceCancelDTO(@NotBlank(message = "Reason is required") String reason) {
}
