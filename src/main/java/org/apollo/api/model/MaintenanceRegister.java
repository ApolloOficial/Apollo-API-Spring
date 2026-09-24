package org.apollo.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;
import org.apollo.api.enums.MaintenanceStatusEnum;
import org.apollo.api.enums.PriorityEnum;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "maintenance_register")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceRegister {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_maintenance_id")
    private MaintenanceRegister parentMaintenance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maintenance_type_id", nullable = false)
    private MaintenanceType maintenanceType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    private Batch batch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technician_id", nullable = false)
    private Employee technician;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false, updatable = false)
    private Employee createdBy;

    @Column(name = "technical_report", columnDefinition = "TEXT")
    private String technicalReport;

    @Enumerated(EnumType.STRING)
    @Column(name = "maintenance_status", nullable = false, length = 30)
    private MaintenanceStatusEnum maintenanceStatus = MaintenanceStatusEnum.ABERTA;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 10)
    private PriorityEnum priority = PriorityEnum.MÉDIA;

    @Column(name = "opening_dt", nullable = false)
    private LocalDate openingDt = LocalDate.now();

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "concluded_at")
    private LocalDateTime concludedAt;

    @Column(name = "estimated_cost", precision = 12, scale = 2)
    private BigDecimal estimatedCost;

    @Column(name = "actual_cost", precision = 12, scale = 2)
    private BigDecimal actualCost;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
