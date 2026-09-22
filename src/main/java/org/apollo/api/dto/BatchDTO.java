package org.apollo.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchDTO {

    private UUID id;

    @NotNull(message = "Unidade é obrigatória")
    private UUID companyUnitId;

    @NotBlank(message = "Número da nota fiscal é obrigatório")
    @Size(max = 50)
    private String billNumber;

    @NotBlank(message = "Fabricante é obrigatório")
    @Size(max = 100)
    private String manufacturer;

    @NotBlank(message = "Modelo é obrigatório")
    @Size(max = 100)
    private String model;

    @NotNull(message = "Data de aquisição é obrigatória")
    @PastOrPresent
    private LocalDate acquisitionDt;

    @NotNull(message = "Quantidade de painéis é obrigatória")
    @Positive
    private Integer panelsQtt;

    @DecimalMin(value = "0.0", message = "Custo unitário não pode ser negativo")
    private BigDecimal unitCost;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private java.time.LocalDateTime createdAt;
}