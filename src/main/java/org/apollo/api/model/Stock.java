package org.apollo.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "co_unity_id", nullable = false)
    private CompanyUnit companyUnit;

    @Column(name = "sku", nullable = false, length = 50)
    private String sku;

    @Column(name = "part_name", nullable = false, length = 100)
    private String partName;

    @Column(name = "part_manufacturer", nullable = false, length = 100)
    private String partManufacturer;

    @Column(name = "available_qtt", nullable = false)
    private Integer availableQtt = 0;

    @Column(name = "minimum_qtt", nullable = false)
    private Integer minimumQtt = 0;

    @Column(name = "unit_cost", precision = 12, scale = 2)
    private BigDecimal unitCost;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
}