package org.apollo.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private Long id;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long companyId;

    @NotNull(message = "Perfil é obrigatório")
    @Positive(message = "Perfil deve ser válido")
    private Long roleId;

    private String roleName;

    @NotBlank(message = "Nome completo é obrigatório")
    @Size(max = 100, message = "Nome completo deve ter no máximo 100 caracteres")
    private String fullName;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    @Size(max = 100, message = "Email deve ter no máximo 100 caracteres")
    private String email;

    @NotBlank(message = "CPF é obrigatório")
    @Pattern(
            regexp = "^[0-9]{11}$",
            message = "CPF deve conter exatamente 11 dígitos"
    )
    private String cpf;
}