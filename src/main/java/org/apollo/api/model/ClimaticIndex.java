package org.apollo.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "climatic_index")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClimaticIndex {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "co_unity_id", nullable = false)
    private CompanyUnit companyUnit;

    @Column(name = "measured_at", nullable = false)
    private LocalDateTime measuredAt;

    @Column(name = "irradiation_index", nullable = false, precision = 8, scale = 2)
    private BigDecimal irradiationIndex;

    @Column(name = "avg_temperature", nullable = false, precision = 5, scale = 2)
    private BigDecimal avgTemperature;

    @Column(name = "weather_condition", nullable = false, length = 50)
    private String weatherCondition;
}