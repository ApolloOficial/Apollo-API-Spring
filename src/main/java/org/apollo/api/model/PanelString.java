package org.apollo.api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apollo.api.enums.StringStatusEnum;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

// String de placas ligada a uma entrada (MPPT/entrada) de um inversor. Tabela "string".
@Entity
@Table(name = "string")
@Getter
@Setter
@NoArgsConstructor
public class PanelString {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inverter_id", nullable = false)
    private Inverter inverter;

    @Column(name = "mppt_number", nullable = false)
    private Short mpptNumber;

    @Column(name = "entry_number", nullable = false)
    private Short entryNumber;

    @Column(name = "code", nullable = false, length = 30)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "panel_model_id", nullable = false)
    private PanelModel panelModel;

    @Column(name = "invoice_number", nullable = false, length = 50)
    private String invoiceNumber;

    @Column(name = "acquisition_dt", nullable = false)
    private LocalDate acquisitionDt;

    @Column(name = "unit_cost", precision = 12, scale = 2)
    private BigDecimal unitCost;

    @Column(name = "purchased_qty", nullable = false)
    private Integer purchasedQty;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StringStatusEnum status = StringStatusEnum.EM_ESTOQUE;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
