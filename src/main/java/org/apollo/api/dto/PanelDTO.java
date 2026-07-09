package org.apollo.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apollo.api.enums.OperatingStatsEnum;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PanelDTO {

    private Long id;
    private Long batchId;
    private Integer coUnityId;
    private Integer estimatedLifeCycle;
    private String serialNumber;
    private String barcode;
    private OperatingStatsEnum operatingStatsEnum;
    private BigDecimal ratedEfficiency;
    private LocalDate installationDt;
}
