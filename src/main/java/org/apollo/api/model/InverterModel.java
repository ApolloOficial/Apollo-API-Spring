package org.apollo.api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "inverter_model")
@Getter
@Setter
@NoArgsConstructor
public class InverterModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "brand", nullable = false, length = 60)
    private String brand;

    @Column(name = "model", nullable = false, length = 60)
    private String model;

    @Column(name = "rated_power_kw", nullable = false, precision = 8, scale = 2)
    private BigDecimal ratedPowerKw;

    @Column(name = "mppt_count", nullable = false)
    private Short mpptCount;

    @Column(name = "strings_per_mppt", nullable = false)
    private Short stringsPerMppt;

    @Column(name = "string_current_monitoring", nullable = false)
    private boolean stringCurrentMonitoring = true;
}
