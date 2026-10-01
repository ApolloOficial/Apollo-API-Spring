package org.apollo.api.dto;

import jakarta.validation.constraints.NotBlank;

public record PanelCreateDTO(
        @NotBlank(message = "Serial number is required") String serialNumber,
        @NotBlank(message = "Barcode is required") String barcode) {
}
