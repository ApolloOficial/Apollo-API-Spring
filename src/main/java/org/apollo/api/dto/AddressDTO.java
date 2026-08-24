package org.apollo.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressDTO {

    private Long id;

    @NotBlank(message = "Logradouro é obrigatório")
    private String streetName;

    @NotBlank(message = "Número é obrigatório")
    private String number;

    private String additionalInfo;

    @NotBlank(message = "Bairro é obrigatório")
    private String neighborhood;

    @NotBlank(message = "Cidade é obrigatória")
    private String city;

    @Pattern(regexp = "^[A-Z]{2}$", message = "Estado deve conter exatamente 2 letras maiúsculas")
    private String state;

    @Pattern(regexp = "^[0-9]{8}$", message = "CEP deve conter exatamente 8 dígitos")
    private String zipCode;
}