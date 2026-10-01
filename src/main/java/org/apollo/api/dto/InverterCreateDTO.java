package org.apollo.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record InverterCreateDTO(
        @NotNull(message = "Inverter model is required") Long inverterModelId,
        @NotBlank(message = "Code is required") String code,
        @NotBlank(message = "Serial number is required") String serialNumber,
        @NotNull(message = "Installation date is required") LocalDate installedAt,
        @NotEmpty(message = "Inform at least one string") @Valid List<StringCreateDTO> strings) {
}
