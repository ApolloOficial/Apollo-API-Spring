package org.apollo.api.dto;

import org.apollo.api.enums.MaintenanceStatusEnum;

import java.time.LocalDateTime;
import java.util.List;

// Um no da cadeia de ordens de servico (OS original -> retrabalhos). chainLevel comeca em 1 na raiz;
// path e a lista de ids da raiz ate esta OS.
public record MaintenanceChainDTO(Long id, Long parentMaintenanceId, MaintenanceStatusEnum status,
                                  LocalDateTime openingDt, int chainLevel, List<Long> path) {
}
