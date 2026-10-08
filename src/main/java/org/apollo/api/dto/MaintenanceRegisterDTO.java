package org.apollo.api.dto;

import org.apollo.api.enums.MaintenanceStatusEnum;
import org.apollo.api.enums.PriorityEnum;
import org.apollo.api.enums.WarningTypeEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record MaintenanceRegisterDTO(Long id, Long parentMaintenanceId, Long warningId, WarningTypeEnum warningType,
                                     PriorityEnum priority, UUID companyUnitId, Long maintenanceTypeId,
                                     String maintenanceTypeName, UUID technicianId, String technicianName,
                                     UUID createdById, String technicalReport,
                                     MaintenanceStatusEnum maintenanceStatus, boolean overdue,
                                     LocalDateTime openingDt, LocalDateTime dueDate, LocalDateTime concludedAt,
                                     LocalDateTime cancelledAt, String cancellationReason,
                                     BigDecimal estimatedCost, BigDecimal laborCost, BigDecimal partsCost,
                                     BigDecimal totalCost) {
}
