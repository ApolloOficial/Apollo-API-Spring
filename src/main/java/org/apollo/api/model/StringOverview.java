package org.apollo.api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

// Leitura da view vw_string_overview: string + capacidade + saude atual + contagens.
@Entity
@Immutable
@Table(name = "vw_string_overview")
@Getter
@NoArgsConstructor
public class StringOverview {

    @Id
    @Column(name = "string_id")
    private UUID stringId;

    @Column(name = "company_unit_id")
    private UUID companyUnitId;

    @Column(name = "code")
    private String code;

    @Column(name = "invoice_number")
    private String invoiceNumber;

    @Column(name = "manufacturer")
    private String manufacturer;

    @Column(name = "model")
    private String model;

    @Column(name = "purchased_qty")
    private Integer purchasedQty;

    @Column(name = "installed_panels")
    private Long installedPanels;

    @Column(name = "nominal_power_wp")
    private BigDecimal nominalPowerWp;

    @Column(name = "nominal_power_kwp")
    private BigDecimal nominalPowerKwp;

    @Column(name = "status")
    private String status;

    @Column(name = "health_score")
    private BigDecimal healthScore;

    @Column(name = "health_calculated_at")
    private LocalDateTime healthCalculatedAt;

    @Column(name = "inverter_code")
    private String inverterCode;

    @Column(name = "mppt_number")
    private Short mpptNumber;

    @Column(name = "entry_number")
    private Short entryNumber;

    @Column(name = "panels_operational")
    private Long panelsOperational;

    @Column(name = "panels_in_maintenance")
    private Long panelsInMaintenance;

    @Column(name = "panels_in_stock")
    private Long panelsInStock;

    @Column(name = "active_warnings")
    private Long activeWarnings;

    @Column(name = "last_measurement_at")
    private LocalDateTime lastMeasurementAt;
}
