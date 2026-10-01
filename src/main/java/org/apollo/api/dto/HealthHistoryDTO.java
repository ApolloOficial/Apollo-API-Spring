package org.apollo.api.dto;

import org.apollo.api.model.StringHealthHistory;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record HealthHistoryDTO(Long id, LocalDateTime calculatedAt, Short windowDays, BigDecimal siblingRatio,
                               BigDecimal nominalRatio, BigDecimal warningPenalty, BigDecimal healthScore) {

    public static HealthHistoryDTO of(StringHealthHistory h) {
        return new HealthHistoryDTO(h.getId(), h.getCalculatedAt(), h.getWindowDays(), h.getSiblingRatio(),
                h.getNominalRatio(), h.getWarningPenalty(), h.getHealthScore());
    }
}
