package org.apollo.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchDTO {

    private Long id;
    private String billNumber;
    private String manufacturer;
    private String model;
    private LocalDate acquisitionDt;
    private Integer panelsQtt;
}
