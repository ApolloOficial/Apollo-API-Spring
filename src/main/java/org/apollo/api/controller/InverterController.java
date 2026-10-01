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
@RequestMapping("/api/v1/inverters")
@RequiredArgsConstructor
@Tag(name = "Inverters", description = "Inversores (cadastro cria inversor + strings + placas)")
@SecurityRequirement(name = "bearer-key")
public class InverterController {

    private final InverterService inverterService;

    public enum SortField { code, installedAt, status, createdAt }

    @GetMapping
    @Operation(summary = "List inverters (paginated) with optional filters")
    public Page<InverterDTO> findAll(
            @RequestParam(required = false) UUID companyUnitId,
            @RequestParam(required = false) InverterStatusEnum status,
            @RequestParam(required = false) Long inverterModelId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "code") SortField sortBy,
            @RequestParam(defaultValue = "ASC") Sort.Direction direction) {
        return inverterService.findAll(companyUnitId, status, inverterModelId, search,
                PageParams.of(page, size, direction, sortBy.name()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Find inverter by ID")
    public InverterDTO findById(@PathVariable UUID id) {
        return inverterService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register inverter with its strings and panels (pr_register_inverter)")
    public InverterDTO create(@Valid @RequestBody InverterCreateDTO dto) {
        return inverterService.create(dto);
    }
}
