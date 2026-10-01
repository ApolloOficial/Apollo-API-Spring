package org.apollo.api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "company_unit")
@Getter
@Setter
@NoArgsConstructor
public class CompanyUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "segment_id", nullable = false)
    private Segment segment;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id", nullable = false, unique = true)
    private Address address;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "cnpj", nullable = false, length = 14)
    private String cnpj;

    @Column(name = "email", nullable = false, length = 120)
    private String email;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "contact_email", length = 120)
    private String contactEmail;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsible_employee_id")
    private Employee responsibleEmployee;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt = LocalDate.now();

    // A potencia instalada (kWp) nao e coluna: vem da view vw_company_unit_capacity.
    @Formula("(select c.kwp_total from vw_company_unit_capacity c where c.company_unit_id = id)")
    private BigDecimal kwpTotal;
}
