package org.apollo.api.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import org.apollo.api.enums.PhoneChangeStatusEnum;

public record PhoneChangeRequestDTO(UUID id, UUID employeeId, String employeeName, String employeeEmail,
                                    String newPhone, PhoneChangeStatusEnum status, LocalDateTime createdAt,
                                    LocalDateTime reviewedAt) {
}
