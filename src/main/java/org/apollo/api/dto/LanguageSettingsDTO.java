package org.apollo.api.dto;

import java.util.List;

public record LanguageSettingsDTO(String selected, List<LanguageOptionDTO> options) {
}
