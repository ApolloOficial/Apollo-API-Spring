package org.apollo.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record SuggestedInternalRelocationCreateDTO(
        @NotNull(message = "String is required") UUID stringId,
        @NotNull(message = "Quantity is required") @Positive(message = "Quantity must be positive") Integer quantity,
        @NotNull(message = "Destination unit is required") UUID suggestedUnitId,
        @NotBlank(message = "Justification is required") String justification) {
}
