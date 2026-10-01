package org.apollo.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MaintenancePartDTO(Long partId, String sku, String partName, Integer quantity, BigDecimal unitCost,
                                 BigDecimal totalCost, LocalDateTime consumedAt) {
}
