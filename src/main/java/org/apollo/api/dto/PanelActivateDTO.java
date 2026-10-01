package org.apollo.api.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

// installationDt e opcional: sem ele vale a data de hoje.
public record PanelActivateDTO(
        @NotBlank(message = "Barcode is required") String barcode,
        LocalDate installationDt) {
}
