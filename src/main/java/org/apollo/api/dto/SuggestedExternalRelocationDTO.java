package org.apollo.api.dto;

import org.apollo.api.enums.RelocationStatusEnum;

import java.time.LocalDateTime;
import java.util.UUID;

public record SuggestedExternalRelocationDTO(Long id, Long panelId, String panelSerial, UUID originUnitId,
                                             String originUnitName, Long destinationCompanyId,
                                             String destinationCompanyName, Long segmentId, String segmentName,
                                             UUID requestedById, String requestedByName, UUID reviewedById,
                                             String reviewedByName, String justification,
                                             RelocationStatusEnum status, LocalDateTime suggestedAt,
                                             LocalDateTime reviewedAt) {
}
