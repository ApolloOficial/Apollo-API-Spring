package org.apollo.api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apollo.api.enums.InverterStatusEnum;
import org.hibernate.annotations.Formula;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

// Inversor da filial. Criado pelo operador junto com suas strings e placas (pr_register_inverter).
@Entity
@Table(name = "inverter")
@Getter
@Setter
@NoArgsConstructor
public class Inverter {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_unit_id", nullable = false)
    private CompanyUnit companyUnit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inverter_model_id", nullable = false)
    private InverterModel inverterModel;

    @Column(name = "code", nullable = false, length = 30)
    private String code;

    @Column(name = "serial_number", nullable = false, length = 60)
    private String serialNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private InverterStatusEnum status = InverterStatusEnum.ATIVO;

    @Column(name = "installed_at", nullable = false)
    private LocalDate installedAt;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Formula("(select count(*) from string s where s.inverter_id = id)")
    private Long stringsCount;
}
