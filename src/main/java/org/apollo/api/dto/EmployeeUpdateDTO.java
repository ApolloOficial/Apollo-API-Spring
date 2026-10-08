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

    private UUID companyUnitId;

    @NotNull(message = "Status is required")
    private Boolean active;
}