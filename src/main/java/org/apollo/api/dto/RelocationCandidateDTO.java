package org.apollo.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

// Strings com saude abaixo do limiar: candidatas a realocacao (fn_relocation_candidates).
public record RelocationCandidateDTO(UUID stringId, String stringCode, String inverterCode, UUID companyUnitId,
                                     String unitName, String manufacturer, String model, Long installedPanels,
                                     BigDecimal nominalPowerWp, BigDecimal healthScore,
                                     LocalDateTime healthCalculatedAt, Long ranking) {
}
