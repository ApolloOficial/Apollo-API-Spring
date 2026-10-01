package org.apollo.api.dto;

import jakarta.validation.constraints.NotBlank;

public record PanelDeactivateDTO(
        @NotBlank(message = "Barcode is required") String barcode,
        @NotBlank(message = "Reason is required") String reason) {
}
