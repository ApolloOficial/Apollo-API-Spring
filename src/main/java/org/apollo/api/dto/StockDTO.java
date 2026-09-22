package org.apollo.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockDTO {
    private Long id;

    @NotNull(message = "Unidade é obrigatória")
    private UUID companyUnitId;

    @NotBlank private String sku;
    @NotBlank private String partName;
    @NotBlank private String partManufacturer;

    @NotNull @Min(0)
    private Integer availableQtt;

    @NotNull @Min(0)
    private Integer minimumQtt;

    @DecimalMin("0.0")
    private BigDecimal unitCost;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private java.time.LocalDateTime updatedAt;
}