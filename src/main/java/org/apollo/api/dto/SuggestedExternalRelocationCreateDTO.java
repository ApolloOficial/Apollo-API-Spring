package org.apollo.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SuggestedExternalRelocationCreateDTO(
        @NotNull(message = "Panel is required") Long panelId,
        @NotNull(message = "Destination company is required") Long destinationCompanyId,
        @NotNull(message = "Segment is required") Long segmentId,
        @NotBlank(message = "Justification is required") String justification) {
}
