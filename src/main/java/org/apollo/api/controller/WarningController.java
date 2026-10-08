package org.apollo.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.*;
import org.apollo.api.enums.*;
import org.apollo.api.service.*;
import org.apollo.api.util.PageParams;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/warnings")
@RequiredArgsConstructor
@Tag(name = "Warnings", description = "Central de alertas (gerados pelo banco e pelo tecnico)")
@SecurityRequirement(name = "bearer-key")
public class WarningController {

    private final WarningService warningService;
    private final MaintenanceRegisterService maintenanceRegisterService;

    public enum SortField { id, generationDt, severity, type, status }

    @GetMapping
    @Operation(summary = "List warnings (paginated) with optional filters")
    public Page<WarningDTO> findAll(
            @RequestParam(required = false) PriorityEnum severity,
            @RequestParam(required = false) WarningTypeEnum type,
            @RequestParam(required = false) WarningStatusEnum status,
            @RequestParam(required = false) UUID companyUnitId,
            @RequestParam(required = false) UUID stringId,
            @RequestParam(required = false) Long panelId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "generationDt") SortField sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        return warningService.findAll(severity, type, status, companyUnitId, stringId, panelId,
                PageParams.of(page, size, direction, sortBy.name()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Find warning by ID")
    public WarningDTO findById(@PathVariable Long id) {
        return warningService.findById(id);
    }

    @PostMapping("/{id}/service-order")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Operator opens a service order from an active warning (pr_open_maintenance)")
    public MaintenanceRegisterDTO openServiceOrder(@PathVariable Long id,
                                                   @Valid @RequestBody ServiceOrderCreateDTO dto) {
        return maintenanceRegisterService.create(new MaintenanceCreateDTO(
                id, dto.technicianId(), dto.maintenanceTypeId(), dto.dueDate(), null, null));
    }
}
