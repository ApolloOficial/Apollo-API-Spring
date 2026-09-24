package org.apollo.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeUpdateDTO {

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 120, message = "Nome deve ter no máximo 120 caracteres")
    private String fullName;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    @Size(max = 120, message = "Email deve ter no máximo 120 caracteres")
    private String email;

    @NotNull(message = "Cargo (role) é obrigatório")
    @Positive(message = "Cargo deve ser válido")
    private Long roleId;

    private UUID companyUnitId;

    @NotNull(message = "Status é obrigatório")
    private Boolean active;
}