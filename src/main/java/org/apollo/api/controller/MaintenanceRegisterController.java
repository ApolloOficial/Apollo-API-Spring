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
@RequestMapping("/api/v1/maintenance-registers")
@RequiredArgsConstructor
@Tag(name = "Maintenance Registers", description = "Ordens de servico")
@SecurityRequirement(name = "bearer-key")
public class MaintenanceRegisterController {

    private final MaintenanceRegisterService service;

    public enum SortField { id, openingDt, dueDate, concludedAt, maintenanceStatus }

    @GetMapping
    @Operation(summary = "List maintenance orders (paginated) with optional filters")
    public Page<MaintenanceRegisterDTO> findAll(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) UUID technicianId,
            @RequestParam(required = false) Long maintenanceTypeId,
            @RequestParam(required = false) MaintenanceStatusEnum status,
            @RequestParam(required = false) PriorityEnum priority,
            @RequestParam(required = false) Boolean thisMonth,
            @RequestParam(required = false) Boolean overdue,
            @RequestParam(required = false) UUID companyUnitId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "openingDt") SortField sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        return service.findAll(id, technicianId, maintenanceTypeId, status, priority, thisMonth, overdue,
                companyUnitId, PageParams.of(page, size, direction, sortBy.name()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Find maintenance order by ID")
    public MaintenanceRegisterDTO findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @GetMapping("/{id}/chain")
    @Operation(summary = "Chain of related maintenance orders (root to rework), with chainLevel and path")
    public List<MaintenanceChainDTO> chain(@PathVariable Long id) {
        return service.chain(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Open a maintenance order from an active warning (pr_open_maintenance)")
    public MaintenanceRegisterDTO create(@Valid @RequestBody MaintenanceCreateDTO dto) {
        return service.create(dto);
    }

    @PatchMapping("/{id}/start")
    @Operation(summary = "Technician starts the order (pr_start_maintenance)")
    public MaintenanceRegisterDTO start(@PathVariable Long id) {
        return service.start(id);
    }

    @PatchMapping("/{id}/complete")
    @Operation(summary = "Technician completes the order (pr_complete_maintenance)")
    public MaintenanceRegisterDTO complete(@PathVariable Long id, @Valid @RequestBody MaintenanceCompleteDTO dto) {
        return service.complete(id, dto);
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel the order (pr_cancel_maintenance)")
    public MaintenanceRegisterDTO cancel(@PathVariable Long id, @Valid @RequestBody MaintenanceCancelDTO dto) {
        return service.cancel(id, dto);
    }

    @GetMapping("/{id}/parts")
    @Operation(summary = "Parts consumed by the order")
    public List<MaintenancePartDTO> parts(@PathVariable Long id) {
        return service.parts(id);
    }

    @PostMapping("/{id}/parts")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a part used in the order; stock is debited by trigger (pr_add_maintenance_part)")
    public List<MaintenancePartDTO> addPart(@PathVariable Long id, @Valid @RequestBody MaintenancePartCreateDTO dto) {
        return service.addPart(id, dto);
    }
}
