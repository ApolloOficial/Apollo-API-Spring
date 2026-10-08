package org.apollo.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record StringCreateDTO(
        @NotNull(message = "MPPT number is required") @Positive(message = "MPPT number must be positive") Short mpptNumber,
        @NotNull(message = "Entry number is required") @Positive(message = "Entry number must be positive") Short entryNumber,
        @NotNull(message = "Panel model is required") Long panelModelId,
        @NotBlank(message = "Invoice number is required") String invoiceNumber,
        @NotNull(message = "Acquisition date is required") LocalDate acquisitionDt,
        @DecimalMin(value = "0.0", message = "Unit cost cannot be negative") BigDecimal unitCost,
        @NotEmpty(message = "Inform at least one panel") @Valid List<PanelCreateDTO> panels) {
}
