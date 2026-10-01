package org.apollo.api.dto;

import org.apollo.api.enums.PriorityEnum;
import org.apollo.api.enums.WarningStatusEnum;
import org.apollo.api.enums.WarningTypeEnum;

import java.time.LocalDateTime;
import java.util.UUID;

public record WarningDTO(Long id, UUID stringId, String stringCode, String inverterCode, Long panelId,
                         String panelSerial, UUID companyUnitId, WarningTypeEnum type, PriorityEnum severity,
                         WarningStatusEnum status, String message, UUID reportedBy, LocalDateTime generationDt,
                         LocalDateTime resolvedAt, Long maintenanceId) {
}
