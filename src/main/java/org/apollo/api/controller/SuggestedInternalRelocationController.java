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
@RequestMapping("/api/v1/relocations/internal")
@RequiredArgsConstructor
@Tag(name = "Internal Relocations", description = "Realocacao de placas entre filiais da mesma empresa")
@SecurityRequirement(name = "bearer-key")
public class SuggestedInternalRelocationController {

    private final SuggestedInternalRelocationService service;

    public enum SortField { id, suggestedAt, status }

    @GetMapping
    @Operation(summary = "List internal relocations (paginated)")
    public Page<SuggestedInternalRelocationDTO> findAll(
            @RequestParam(required = false) RelocationStatusEnum status,
            @RequestParam(required = false) UUID originUnitId,
            @RequestParam(required = false) UUID destinationUnitId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "suggestedAt") SortField sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        return service.findAll(status, originUnitId, destinationUnitId,
                PageParams.of(page, size, direction, sortBy.name()));
    }

    @GetMapping("/incoming")
    @Operation(summary = "Relocations suggested to the authenticated user's unit (optional status filter)")
    public Page<SuggestedInternalRelocationDTO> incoming(
            @RequestParam(required = false) RelocationStatusEnum status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "suggestedAt") SortField sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        return service.findIncoming(status, PageParams.of(page, size, direction, sortBy.name()));
    }

    @GetMapping("/mine")
    @Operation(summary = "Relocations suggested by the authenticated user (optional status filter)")
    public Page<SuggestedInternalRelocationDTO> mine(
            @RequestParam(required = false) RelocationStatusEnum status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "suggestedAt") SortField sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        return service.findMine(status, PageParams.of(page, size, direction, sortBy.name()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Find internal relocation by ID")
    public SuggestedInternalRelocationDTO findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Operator suggests an internal relocation")
    public SuggestedInternalRelocationDTO create(@Valid @RequestBody SuggestedInternalRelocationCreateDTO dto) {
        return service.create(dto);
    }

    @PatchMapping("/{id}/review")
    @Operation(summary = "Manager approves or rejects (status APROVADA/REJEITADA)")
    public SuggestedInternalRelocationDTO review(@PathVariable Long id, @Valid @RequestBody RelocationReviewDTO dto) {
        return service.review(id, dto.status());
    }

    @PatchMapping("/{id}/decision")
    @Operation(summary = "Manager decision (same as /review): status APROVADA/REJEITADA, reviewer = authenticated user")
    public SuggestedInternalRelocationDTO decision(@PathVariable Long id, @Valid @RequestBody RelocationReviewDTO dto) {
        return service.review(id, dto.status());
    }

    @PatchMapping("/{id}/complete")
    @Operation(summary = "Mark an approved relocation as completed")
    public SuggestedInternalRelocationDTO complete(@PathVariable Long id) {
        return service.complete(id);
    }
}
