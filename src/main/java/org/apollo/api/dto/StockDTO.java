package org.apollo.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record StockDTO(UUID companyUnitId, String companyUnitName, Long partId, String sku, String partName,
                       String manufacturer, Integer availableQtt, Integer minimumQtt, BigDecimal unitCost,
                       LocalDateTime updatedAt, boolean belowMinimum) {
}
