package org.apollo.api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apollo.api.enums.RelocationStatusEnum;

import java.time.LocalDateTime;

// Sugestao de mover placas de uma string para outra filial da MESMA empresa.
@Entity
@Table(name = "suggested_internal_relocation")
@Getter
@Setter
@NoArgsConstructor
public class SuggestedInternalRelocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "string_id", nullable = false)
    private PanelString panelString;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "suggested_unit_id", nullable = false)
    private CompanyUnit suggestedUnit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by", nullable = false)
    private Employee requestedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private Employee reviewedBy;

    @Column(name = "justification", nullable = false, columnDefinition = "TEXT")
    private String justification;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RelocationStatusEnum status = RelocationStatusEnum.PENDENTE;

    @Column(name = "suggested_at", nullable = false)
    private LocalDateTime suggestedAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;
}
