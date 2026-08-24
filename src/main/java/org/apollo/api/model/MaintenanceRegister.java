package org.apollo.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apollo.api.enums.MaintenanceStatusEnum;

import java.time.LocalDate;

@Entity
@Table(name = "Maintenance_Register")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceRegister {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maintenance_type_id", nullable = false)
    private MaintenanceType maintenanceType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "panel_id", nullable = false)
    private Panel panel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technician_id", nullable = false)
    private MaintenanceTechnician technician;

    @Column(name = "technical_report", nullable = false)
    private String technicalReport;

    @Column(name = "maintenance_status", nullable = false, length = 30)
    private MaintenanceStatusEnum maintenanceStatus;

    @Column(name = "opening_dt", nullable = false)
    private LocalDate openingDt;

    @Column(name = "conclusion_dt")
    private LocalDate conclusionDt;
}