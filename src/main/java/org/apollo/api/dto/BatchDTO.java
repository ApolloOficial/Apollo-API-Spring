package org.apollo.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchDTO {

    private Long id;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long companyId;

    @NotBlank(message = "Número da nota fiscal é obrigatório")
    @Size(max = 50, message = "Número da nota fiscal deve ter no máximo 50 caracteres")
    private String billNumber;

    @NotBlank(message = "Fabricante é obrigatório")
    @Size(max = 100, message = "Fabricante deve ter no máximo 100 caracteres")
    private String manufacturer;

    @NotBlank(message = "Modelo é obrigatório")
    @Size(max = 100, message = "Modelo deve ter no máximo 100 caracteres")
    private String model;

    @NotNull(message = "Data de aquisição é obrigatória")
    @PastOrPresent(message = "Data de aquisição não pode estar no futuro")
    private LocalDate acquisitionDt;

    @NotNull(message = "Quantidade de painéis é obrigatória")
    @Positive(message = "Quantidade de painéis deve ser maior que zero")
    private Integer panelsQtt;
}
