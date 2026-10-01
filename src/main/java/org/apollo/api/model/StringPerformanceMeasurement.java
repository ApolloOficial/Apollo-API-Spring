package org.apollo.api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apollo.api.enums.DataSourceEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

// Leitura de potencia de uma string (so leitura na API; a ingestao usa pr_ingest_string_measurement).
@Entity
@Table(name = "string_performance_measurement")
@Getter
@Setter
@NoArgsConstructor
public class StringPerformanceMeasurement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "string_id", nullable = false)
    private UUID stringId;

    @Column(name = "measured_at", nullable = false)
    private LocalDateTime measuredAt;

    @Column(name = "power_w", nullable = false, precision = 10, scale = 2)
    private BigDecimal powerW;

    @Column(name = "voltage_v", precision = 8, scale = 2)
    private BigDecimal voltageV;

    @Column(name = "current_a", precision = 8, scale = 2)
    private BigDecimal currentA;

    @Column(name = "generated_energy_kwh", precision = 10, scale = 3)
    private BigDecimal generatedEnergyKwh;

    @Column(name = "fault_code", length = 30)
    private String faultCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "data_source", nullable = false, length = 20)
    private DataSourceEnum dataSource;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
