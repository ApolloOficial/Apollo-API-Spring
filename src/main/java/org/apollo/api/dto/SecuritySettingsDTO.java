package org.apollo.api.dto;

import java.time.LocalDateTime;

public record SecuritySettingsDTO(String maskedPhone, boolean phoneChangePending, LocalDateTime passwordChangedAt) {
}
