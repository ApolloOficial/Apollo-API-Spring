package org.apollo.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

// companyUnitId e opcional: sem ele vale a filial do usuario logado.
public record StockCreateDTO(
        UUID companyUnitId,
        @NotNull(message = "Part is required") Long partId,
        @NotNull(message = "Available quantity is required") @Min(value = 0, message = "Quantity cannot be negative") Integer availableQtt,
        @NotNull(message = "Minimum quantity is required") @Min(value = 0, message = "Quantity cannot be negative") Integer minimumQtt,
        @DecimalMin(value = "0.0", message = "Unit cost cannot be negative") BigDecimal unitCost) {
}
