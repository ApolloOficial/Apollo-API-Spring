package org.apollo.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record StockUpdateDTO(
        @NotNull(message = "Available quantity is required") @Min(value = 0, message = "Quantity cannot be negative") Integer availableQtt,
        @NotNull(message = "Minimum quantity is required") @Min(value = 0, message = "Quantity cannot be negative") Integer minimumQtt,
        @DecimalMin(value = "0.0", message = "Unit cost cannot be negative") BigDecimal unitCost) {
}
