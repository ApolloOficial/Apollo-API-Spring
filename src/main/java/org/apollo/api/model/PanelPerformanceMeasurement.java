package org.apollo.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apollo.api.enums.DataSourceEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "panel_performance_measurement")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PanelPerformanceMeasurement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "panel_id", nullable = false)
    private Panel panel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "climatic_index_id")
    private ClimaticIndex climaticIndex;

    @Column(name = "measured_at", nullable = false)
    private LocalDateTime measuredAt;

    @Column(name = "generated_energy_kwh", nullable = false, precision = 12, scale = 3)
    private BigDecimal generatedEnergyKwh;

    @Column(name = "current_efficiency", nullable = false, precision = 5, scale = 2)
    private BigDecimal currentEfficiency;

    @Column(name = "health_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal healthScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "data_source", nullable = false, length = 30)
    private DataSourceEnum dataSource = DataSourceEnum.SENSOR;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}