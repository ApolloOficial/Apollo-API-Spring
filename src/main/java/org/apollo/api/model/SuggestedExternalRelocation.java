package org.apollo.api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apollo.api.enums.RelocationStatusEnum;

import java.time.LocalDateTime;

// Sugestao de doar/mover UMA placa para outra empresa.
@Entity
@Table(name = "suggested_external_relocation")
@Getter
@Setter
@NoArgsConstructor
public class SuggestedExternalRelocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "panel_id", nullable = false)
    private Panel panel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_company_id", nullable = false)
    private Company destinationCompany;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "segment_id", nullable = false)
    private Segment segment;

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
