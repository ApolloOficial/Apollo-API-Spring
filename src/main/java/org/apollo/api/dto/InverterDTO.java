package org.apollo.api.dto;

import org.apollo.api.enums.InverterStatusEnum;

import java.time.LocalDate;
import java.util.UUID;

public record InverterDTO(UUID id, UUID companyUnitId, String companyUnitName, Long inverterModelId,
                          String brand, String model, String code, String serialNumber,
                          InverterStatusEnum status, LocalDate installedAt, Long stringsCount) {
}
