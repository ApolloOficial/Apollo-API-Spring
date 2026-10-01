package org.apollo.api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apollo.api.enums.MaintenanceStatusEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

// Ordem de servico (OS). Sempre nasce de um alerta (warning_id unico).
@Entity
@Table(name = "maintenance_register")
@Getter
@Setter
@NoArgsConstructor
public class MaintenanceRegister {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_maintenance_id")
    private MaintenanceRegister parentMaintenance;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warning_id", nullable = false)
    private Warning warning;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maintenance_type_id", nullable = false)
    private MaintenanceType maintenanceType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technician_id", nullable = false)
    private Employee technician;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private Employee createdBy;

    @Column(name = "technical_report", columnDefinition = "TEXT")
    private String technicalReport;

    @Enumerated(EnumType.STRING)
    @Column(name = "maintenance_status", nullable = false, length = 20)
    private MaintenanceStatusEnum maintenanceStatus = MaintenanceStatusEnum.ABERTA;

    @Column(name = "opening_dt", nullable = false)
    private LocalDateTime openingDt;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "concluded_at")
    private LocalDateTime concludedAt;

    @Column(name = "cancelled_by")
    private UUID cancelledBy;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @Column(name = "estimated_cost", precision = 12, scale = 2)
    private BigDecimal estimatedCost;

    @Column(name = "labor_cost", precision = 12, scale = 2)
    private BigDecimal laborCost;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
