package org.apollo.api.dto;

import org.apollo.api.enums.OperatingStatsEnum;
import org.apollo.api.model.PanelOverview;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record PanelDTO(Long id, UUID stringId, String stringCode, String inverterCode, UUID companyUnitId,
                       String serialNumber, String barcode, String manufacturer, String model,
                       OperatingStatsEnum operatingStats, LocalDate installationDt, LocalDateTime activatedAt,
                       BigDecimal stringHealthScore, Long activeWarnings) {

    public static PanelDTO of(PanelOverview p) {
        return new PanelDTO(p.getPanelId(), p.getStringId(), p.getStringCode(), p.getInverterCode(),
                p.getCompanyUnitId(), p.getSerialNumber(), p.getBarcode(), p.getManufacturer(), p.getModel(),
                p.getOperatingStats(), p.getInstallationDt(), p.getActivatedAt(), p.getStringHealthScore(),
                p.getActivePanelWarnings());
    }
}
