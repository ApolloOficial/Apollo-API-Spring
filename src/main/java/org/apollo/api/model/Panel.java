package org.apollo.api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apollo.api.enums.OperatingStatsEnum;

import java.time.LocalDate;
import java.time.LocalDateTime;

// Placa individual. Ativada pelo tecnico (bipe do codigo de barras) via pr_activate_panel.
@Entity
@Table(name = "panel")
@Getter
@Setter
@NoArgsConstructor
public class Panel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "string_id", nullable = false)
    private PanelString panelString;

    @Column(name = "serial_number", nullable = false, length = 100)
    private String serialNumber;

    @Column(name = "barcode", nullable = false, length = 100)
    private String barcode;

    @Enumerated(EnumType.STRING)
    @Column(name = "operating_stats", nullable = false, length = 20)
    private OperatingStatsEnum operatingStats = OperatingStatsEnum.EM_ESTOQUE;

    @Column(name = "installation_dt")
    private LocalDate installationDt;

    @Column(name = "activated_at")
    private LocalDateTime activatedAt;

    @Column(name = "deactivated_at")
    private LocalDateTime deactivatedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
