package org.apollo.api.dto;

import java.util.UUID;

public record SettingsSummaryDTO(UUID userId, String fullName, String email, String role, String roleLabel,
                                 String unitName, boolean hasPhoto, String photoUrl, String language) {
}
