package org.apollo.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apollo.api.enums.RelocationStatusEnum;

import java.time.LocalDateTime;

@Entity
@Table(name = "suggested_external_relocation")
@Data
@NoArgsConstructor
@AllArgsConstructor
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

    @Column(name = "justification", nullable = false)
    private String justification;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RelocationStatusEnum status = RelocationStatusEnum.PENDENTE;

    @Column(name = "suggested_at", nullable = false, updatable = false)
    private LocalDateTime suggestedAt = LocalDateTime.now();

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;
}