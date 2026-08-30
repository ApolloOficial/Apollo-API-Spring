package org.apollo.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyUnitDTO {

    private Long id;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long companyId;

    @NotNull(message = "Segmento é obrigatório")
    private Long segmentId;

    private String segmentName;

    @NotNull(message = "Endereço é obrigatório")
    @Valid
    private AddressDTO address;

    @NotBlank(message = "Nome é obrigatório")
    private String name;

    private LocalDate createdAt;

    @NotBlank(message = "Email é obrigatório")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "Email inválido")
    private String email;

    @Pattern(regexp = "^[0-9]{10,11}$", message = "Telefone deve conter 10 ou 11 dígitos")
    private String phone;
}
