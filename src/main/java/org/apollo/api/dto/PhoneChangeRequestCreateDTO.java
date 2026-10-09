package org.apollo.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhoneChangeRequestCreateDTO {

    @NotBlank(message = "New phone is required")
    @Size(max = 25, message = "New phone is too long")
    private String newPhone;
}
