package org.apollo.api.dto;

import java.math.BigDecimal;

public record InverterModelDTO(Long id, String brand, String model, BigDecimal ratedPowerKw,
                               Short mpptCount, Short stringsPerMppt, boolean stringCurrentMonitoring) {
}
