package org.apollo.api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

// Historico do indice de saude da string (so leitura; calculado por pr_recalculate_string_health).
@Entity
@Table(name = "string_health_history")
@Getter
@Setter
@NoArgsConstructor
public class StringHealthHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "string_id", nullable = false)
    private UUID stringId;

    @Column(name = "calculated_at", nullable = false)
    private LocalDateTime calculatedAt;

    @Column(name = "window_days", nullable = false)
    private Short windowDays;

    @Column(name = "sibling_ratio", precision = 6, scale = 4)
    private BigDecimal siblingRatio;

    @Column(name = "nominal_ratio", nullable = false, precision = 6, scale = 4)
    private BigDecimal nominalRatio;

    @Column(name = "warning_penalty", nullable = false, precision = 5, scale = 2)
    private BigDecimal warningPenalty;

    @Column(name = "health_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal healthScore;
}
