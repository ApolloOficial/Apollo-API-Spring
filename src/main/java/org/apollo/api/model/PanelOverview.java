package org.apollo.api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apollo.api.enums.OperatingStatsEnum;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

// Leitura da view vw_string_panels: placa + string + inversor + modelo + saude da string.
@Entity
@Immutable
@Table(name = "vw_string_panels")
@Getter
@NoArgsConstructor
public class PanelOverview {

    @Id
    @Column(name = "panel_id")
    private Long panelId;

    @Column(name = "string_id")
    private UUID stringId;

    @Column(name = "company_unit_id")
    private UUID companyUnitId;

    @Column(name = "string_code")
    private String stringCode;

    @Column(name = "inverter_code")
    private String inverterCode;

    @Column(name = "serial_number")
    private String serialNumber;

    @Column(name = "barcode")
    private String barcode;

    @Column(name = "manufacturer")
    private String manufacturer;

    @Column(name = "model")
    private String model;

    @Enumerated(EnumType.STRING)
    @Column(name = "operating_stats")
    private OperatingStatsEnum operatingStats;

    @Column(name = "installation_dt")
    private LocalDate installationDt;

    @Column(name = "activated_at")
    private LocalDateTime activatedAt;

    @Column(name = "string_health_score")
    private BigDecimal stringHealthScore;

    @Column(name = "active_panel_warnings")
    private Long activePanelWarnings;
}
