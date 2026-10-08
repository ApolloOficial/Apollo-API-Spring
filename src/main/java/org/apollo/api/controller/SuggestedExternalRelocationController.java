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
@RequestMapping("/api/v1/relocations/external")
@RequiredArgsConstructor
@Tag(name = "External Relocations", description = "Realocacao de placas para outra empresa")
@SecurityRequirement(name = "bearer-key")
public class SuggestedExternalRelocationController {

    private final SuggestedExternalRelocationService service;

    public enum SortField { id, suggestedAt, status }

    @GetMapping
    @Operation(summary = "List external relocations (paginated)")
    public Page<SuggestedExternalRelocationDTO> findAll(
            @RequestParam(required = false) RelocationStatusEnum status,
            @RequestParam(required = false) UUID originUnitId,
            @RequestParam(required = false) Long destinationCompanyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "suggestedAt") SortField sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        return service.findAll(status, originUnitId, destinationCompanyId,
                PageParams.of(page, size, direction, sortBy.name()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Find external relocation by ID")
    public SuggestedExternalRelocationDTO findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Operator suggests an external relocation")
    public SuggestedExternalRelocationDTO create(@Valid @RequestBody SuggestedExternalRelocationCreateDTO dto) {
        return service.create(dto);
    }

    @PatchMapping("/{id}/review")
    @Operation(summary = "Manager approves or rejects (status APROVADA/REJEITADA)")
    public SuggestedExternalRelocationDTO review(@PathVariable Long id, @Valid @RequestBody RelocationReviewDTO dto) {
        return service.review(id, dto.status());
    }

    @PatchMapping("/{id}/complete")
    @Operation(summary = "Mark an approved relocation as completed")
    public SuggestedExternalRelocationDTO complete(@PathVariable Long id) {
        return service.complete(id);
    }
}
