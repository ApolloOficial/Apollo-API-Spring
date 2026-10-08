package org.apollo.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Tipos de manutencao (Corretiva, Preventiva...) para o filtro "Tipo" e a criacao de OS. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceTypeDTO {
    private Long id;
    private String name;
    private String description;
    private Integer defaultIntervalDays;
}
