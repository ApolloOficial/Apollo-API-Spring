package org.apollo.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apollo.api.enums.PriorityEnum;
import org.apollo.api.enums.WarningStatusEnum;

import java.time.LocalDateTime;

@Entity
@Table(name = "warning")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Warning {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "panel_id", nullable = false)
    private Panel panel;

    @Column(name = "type", nullable = false, length = 50)
    private String type;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 10)
    private PriorityEnum severity = PriorityEnum.MÉDIA;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private WarningStatusEnum status = WarningStatusEnum.ATIVO;

    @Column(name = "message", nullable = false)
    private String message;

    @Column(name = "generation_dt", nullable = false, updatable = false)
    private LocalDateTime generationDt = LocalDateTime.now();

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
}