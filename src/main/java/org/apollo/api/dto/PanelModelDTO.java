package org.apollo.api.dto;

import java.math.BigDecimal;

public record PanelModelDTO(Long id, String manufacturer, String model, BigDecimal pmaxW,
                            BigDecimal efficiencyPct, Short warrantyYears) {
}
