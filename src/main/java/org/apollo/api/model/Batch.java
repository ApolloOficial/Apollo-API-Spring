package org.apollo.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "batch", uniqueConstraints = @UniqueConstraint(name = "uq_batch_bill_company_unit", columnNames = {"company_unit_id", "bill_number"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Batch {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_unit_id", nullable = false)
    private CompanyUnit companyUnit;

    @Column(name = "bill_number", nullable = false, length = 50)
    private String billNumber;

    @Column(name = "manufacturer", nullable = false, length = 100)
    private String manufacturer;

    @Column(name = "model", nullable = false, length = 100)
    private String model;

    @Column(name = "acquisition_dt", nullable = false)
    private LocalDate acquisitionDt;

    @Column(name = "panels_qtt", nullable = false)
    private Integer panelsQtt;

    @Column(name = "unit_cost", precision = 12, scale = 2)
    private BigDecimal unitCost;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
