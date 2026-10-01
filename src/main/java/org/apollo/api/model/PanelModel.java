package org.apollo.api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "panel_model")
@Getter
@Setter
@NoArgsConstructor
public class PanelModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "manufacturer", nullable = false, length = 100)
    private String manufacturer;

    @Column(name = "model", nullable = false, length = 100)
    private String model;

    @Column(name = "pmax_w", nullable = false, precision = 7, scale = 2)
    private BigDecimal pmaxW;

    @Column(name = "efficiency_pct", precision = 5, scale = 2)
    private BigDecimal efficiencyPct;

    @Column(name = "warranty_years")
    private Short warrantyYears;
}
