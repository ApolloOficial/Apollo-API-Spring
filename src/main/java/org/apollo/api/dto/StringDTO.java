package org.apollo.api.dto;

import org.apollo.api.model.StringOverview;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record StringDTO(UUID id, UUID companyUnitId, String code, String inverterCode, Short mpptNumber,
                        Short entryNumber, String invoiceNumber, String manufacturer, String model,
                        Integer purchasedQty, Long installedPanels, BigDecimal nominalPowerKwp, String status,
                        BigDecimal healthScore, LocalDateTime healthCalculatedAt, Long panelsOperational,
                        Long panelsInMaintenance, Long panelsInStock, Long activeWarnings,
                        LocalDateTime lastMeasurementAt) {

    public static StringDTO of(StringOverview s) {
        return new StringDTO(s.getStringId(), s.getCompanyUnitId(), s.getCode(), s.getInverterCode(),
                s.getMpptNumber(), s.getEntryNumber(), s.getInvoiceNumber(), s.getManufacturer(), s.getModel(),
                s.getPurchasedQty(), s.getInstalledPanels(), s.getNominalPowerKwp(), s.getStatus(),
                s.getHealthScore(), s.getHealthCalculatedAt(), s.getPanelsOperational(),
                s.getPanelsInMaintenance(), s.getPanelsInStock(), s.getActiveWarnings(), s.getLastMeasurementAt());
    }
}
