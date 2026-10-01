package org.apollo.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressDTO {

    private Long id;
    private UUID companyUnitId;

    @NotBlank(message = "Street name is required")
    private String streetName;

    @NotBlank(message = "Number is required")
    private String number;

    private String additionalInfo;

    @NotBlank(message = "Neighborhood is required")
    private String neighborhood;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    @Pattern(regexp = "^[A-Z]{2}$", message = "State must contain exactly 2 uppercase letters")
    private String state;

    @NotBlank(message = "ZIP code is required")
    @Pattern(regexp = "^[0-9]{8}$", message = "ZIP code must contain exactly 8 digits")
    private String zipCode;

    public AddressDTO(Long id, String streetName, String number, String additionalInfo,
                      String neighborhood, String city, String state, String zipCode) {
        this(id, null, streetName, number, additionalInfo, neighborhood, city, state, zipCode);
    }
}
