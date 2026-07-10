package org.apollo.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyUnitDTO {

    private Long id;
    private Long segmentId;
    private String segmentName;
    private AddressDTO address;
    private String name;
    private LocalDate createdAt;
    private String email;
    private String phone;
}
