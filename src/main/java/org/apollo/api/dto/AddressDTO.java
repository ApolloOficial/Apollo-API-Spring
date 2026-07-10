package org.apollo.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressDTO {

    private Long id;
    private String streetName;
    private String number;
    private String additionalInfo;
    private String neighborhood;
    private String city;
    private String state;
    private String zipCode;
}