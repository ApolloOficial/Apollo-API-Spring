package org.apollo.api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Estoque de pecas por filial. A chave e (filial, peca).
@Entity
@Table(name = "stock")
@Getter
@Setter
@NoArgsConstructor
public class Stock {

    @EmbeddedId
    private StockId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("companyUnitId")
    @JoinColumn(name = "company_unit_id", nullable = false)
    private CompanyUnit companyUnit;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("partId")
    @JoinColumn(name = "part_id", nullable = false)
    private Part part;

    @Column(name = "available_qtt", nullable = false)
    private Integer availableQtt = 0;

    @Column(name = "minimum_qtt", nullable = false)
    private Integer minimumQtt = 0;

    @Column(name = "unit_cost", precision = 12, scale = 2)
    private BigDecimal unitCost;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
}
