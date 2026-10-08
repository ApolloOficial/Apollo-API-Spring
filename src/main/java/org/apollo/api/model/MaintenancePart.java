package org.apollo.api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Peca consumida numa OS. O trigger do banco baixa o estoque da filial e preenche o custo.
@Entity
@Table(name = "maintenance_part")
@Getter
@Setter
@NoArgsConstructor
public class MaintenancePart {

    @EmbeddedId
    private MaintenancePartId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("partId")
    @JoinColumn(name = "part_id", nullable = false)
    private Part part;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_cost", precision = 12, scale = 2)
    private BigDecimal unitCost;

    @Column(name = "consumed_at", nullable = false)
    private LocalDateTime consumedAt;
}
