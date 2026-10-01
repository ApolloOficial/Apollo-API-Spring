package org.apollo.api.dto;

import org.apollo.api.model.StringPerformanceMeasurement;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MeasurementDTO(Long id, LocalDateTime measuredAt, BigDecimal powerW, BigDecimal voltageV,
                             BigDecimal currentA, BigDecimal generatedEnergyKwh, String faultCode, String dataSource) {

    public static MeasurementDTO of(StringPerformanceMeasurement m) {
        return new MeasurementDTO(m.getId(), m.getMeasuredAt(), m.getPowerW(), m.getVoltageV(), m.getCurrentA(),
                m.getGeneratedEnergyKwh(), m.getFaultCode(), m.getDataSource() == null ? null : m.getDataSource().name());
    }
}
