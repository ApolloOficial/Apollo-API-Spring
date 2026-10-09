package org.apollo.api.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record SessionDTO(UUID id, String deviceName, boolean current, LocalDateTime firstLoginAt,
                         LocalDateTime lastLoginAt, LocalDateTime lastSeenAt) {
}
