package org.apollo.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apollo.api.enums.MaintenanceStatusEnum;
import org.apollo.api.enums.PriorityEnum;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceRegisterDTO {

    private Long id;

    private Long parentMaintenanceId;

    @NotNull(message = "Tipo de manutenção é obrigatório")
    private Long maintenanceTypeId;

    @NotNull(message = "Lote é obrigatório")
    private UUID batchId;

    @NotNull(message = "Técnico é obrigatório")
    private UUID technicianId;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private UUID createdById;

    private String technicalReport;

    @NotNull(message = "Status da manutenção é obrigatório")
    private MaintenanceStatusEnum maintenanceStatus = MaintenanceStatusEnum.ABERTA;

    @NotNull(message = "Prioridade é obrigatória")
    private PriorityEnum priority = PriorityEnum.MÉDIA;

    @NotNull(message = "Data de abertura é obrigatória")
    private LocalDate openingDt = LocalDate.now();

    private LocalDateTime dueDate;

    private LocalDateTime concludedAt;

    @DecimalMin(value = "0.0", message = "Custo estimado não pode ser negativo")
    private BigDecimal estimatedCost;

    @DecimalMin(value = "0.0", message = "Custo real não pode ser negativo")
    private BigDecimal actualCost;
}
