package org.apollo.api.dto;

import org.apollo.api.enums.RelocationStatusEnum;

import java.time.LocalDateTime;
import java.util.UUID;

public record SuggestedInternalRelocationDTO(Long id, UUID stringId, String stringCode, String inverterCode,
                                             UUID originUnitId, String originUnitName, Integer quantity,
                                             UUID suggestedUnitId, String suggestedUnitName, UUID requestedById,
                                             String requestedByName, UUID reviewedById, String reviewedByName,
                                             String justification, RelocationStatusEnum status,
                                             LocalDateTime suggestedAt, LocalDateTime reviewedAt) {
}
