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
@RequestMapping("/api/v1/strings")
@RequiredArgsConstructor
@Tag(name = "Strings", description = "Strings fotovoltaicas, medicoes e saude")
@SecurityRequirement(name = "bearer-key")
public class StringController {

    private final StringService stringService;

    public enum SortField { code, healthScore, inverterCode, status, activeWarnings, lastMeasurementAt }

    @GetMapping
    @Operation(summary = "List strings (paginated) with optional filters")
    public Page<StringDTO> findAll(
            @RequestParam(required = false) UUID companyUnitId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String inverterCode,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) BigDecimal maxHealth,
            @RequestParam(required = false) Boolean withWarnings,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "code") SortField sortBy,
            @RequestParam(defaultValue = "ASC") Sort.Direction direction) {
        return stringService.findAll(companyUnitId, status, inverterCode, search, maxHealth, withWarnings,
                PageParams.of(page, size, direction, sortBy.name()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Find string by ID")
    public StringDTO findById(@PathVariable UUID id) {
        return stringService.findById(id);
    }

    @GetMapping("/{id}/measurements")
    @Operation(summary = "String performance measurements in a period (paginated, newest first)")
    public Page<MeasurementDTO> measurements(
            @PathVariable UUID id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return stringService.measurements(id, from, to, PageParams.of(page, size, Sort.Direction.DESC, "measuredAt"));
    }

    @GetMapping("/{id}/health-history")
    @Operation(summary = "String health score history (paginated, newest first)")
    public Page<HealthHistoryDTO> healthHistory(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return stringService.healthHistory(id, PageParams.of(page, size, Sort.Direction.DESC, "calculatedAt"));
    }

    @GetMapping("/relocation-candidates")
    @Operation(summary = "Strings whose health is below the threshold (fn_relocation_candidates)")
    public List<RelocationCandidateDTO> relocationCandidates(@RequestParam(required = false) BigDecimal threshold) {
        return stringService.relocationCandidates(threshold);
    }

    @PostMapping("/recalculate-health")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Recalculate string health scores (pr_recalculate_health)")
    public void recalculate(@RequestParam(required = false) UUID companyUnitId,
                            @RequestParam(required = false) Integer windowDays,
                            @RequestParam(required = false) BigDecimal threshold) {
        stringService.recalculateHealth(companyUnitId, windowDays, threshold);
    }
}
