package org.apollo.api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apollo.api.enums.PriorityEnum;
import org.apollo.api.enums.WarningStatusEnum;
import org.apollo.api.enums.WarningTypeEnum;

import java.time.LocalDateTime;
import java.util.UUID;

// Alerta. Aponta para uma string (gerado pelo sistema) OU para uma placa (relato do tecnico).
@Entity
@Table(name = "warning")
@Getter
@Setter
@NoArgsConstructor
public class Warning {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "string_id")
    private PanelString panelString;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "panel_id")
    private Panel panel;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private WarningTypeEnum type;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 10)
    private PriorityEnum severity = PriorityEnum.MEDIA;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private WarningStatusEnum status = WarningStatusEnum.ATIVO;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "reported_by")
    private UUID reportedBy;

    @Column(name = "generation_dt", nullable = false)
    private LocalDateTime generationDt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
}
