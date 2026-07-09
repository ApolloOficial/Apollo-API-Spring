package org.apollo.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apollo.api.enums.OperatingStatsEnum;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "Using_Panels")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Panel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    private Batch batch;

    @Column(name = "co_unity_id", nullable = false)
    private Integer coUnityId;

    @Column(name = "estimated_life_cycle", nullable = false)
    private Integer estimatedLifeCycle;

    @Column(name = "serial_number", nullable = false, unique = true, length = 100)
    private String serialNumber;

    @Column(name = "barcode", nullable = false, length = 100)
    private String barcode;

    @Enumerated(EnumType.STRING)
    @Column(name = "operating_stats", nullable = false, length = 30)
    private OperatingStatsEnum operatingStatsEnum;

    @Column(name = "rated_efficiency", nullable = false, precision = 5, scale = 2)
    private BigDecimal ratedEfficiency;

    @Column(name = "installation_dt", nullable = false)
    private LocalDate installationDt;
}
