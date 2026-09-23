package org.apollo.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apollo.api.enums.RelocationStatusEnum;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "suggested_internal_relocation")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SuggestedInternalRelocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    private Batch batch;

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

    @Column(name = "justification", nullable = false)
    private String justification;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RelocationStatusEnum status = RelocationStatusEnum.PENDENTE;

    @Column(name = "suggested_at", nullable = false, updatable = false)
    private LocalDateTime suggestedAt = LocalDateTime.now();

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Transient
    public UUID getBatchId() {
        return batch != null ? batch.getId() : null;
    }
}