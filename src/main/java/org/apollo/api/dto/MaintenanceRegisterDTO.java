package org.apollo.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apollo.api.enums.MaintenanceStatusEnum;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceRegisterDTO {

    private Long id;
    private Long maintenanceTypeId;
    private Long panelId;
    private Long technicianId;
    private String technicalReport;
    private MaintenanceStatusEnum maintenanceStatus;
    private LocalDate openingDt;
    private LocalDate conclusionDt;
}
