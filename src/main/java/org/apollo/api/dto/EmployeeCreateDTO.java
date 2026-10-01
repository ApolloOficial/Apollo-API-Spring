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
public class EmployeeCreateDTO {

    @NotBlank(message = "Name is required")
    @Size(max = 120, message = "Name must be at most 120 characters")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email")
    @Size(max = 120, message = "Email must be at most 120 characters")
    private String email;

    @NotNull(message = "Role is required")
    @Positive(message = "Role must be valid")
    private Long roleId;

    @NotNull(message = "Unit is required")
    private UUID companyUnitId;

    @NotNull(message = "Status is required")
    private Boolean active = true;

    @NotBlank(message = "Temporary password is required")
    @Size(min = 8, max = 72, message = "Temporary password must be between 8 and 72 characters")
    private String temporaryPassword;
}