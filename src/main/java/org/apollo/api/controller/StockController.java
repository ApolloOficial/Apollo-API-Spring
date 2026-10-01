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
@RequestMapping("/api/v1/stocks")
@RequiredArgsConstructor
@Tag(name = "Stocks", description = "Estoque de pecas por filial")
@SecurityRequirement(name = "bearer-key")
public class StockController {

    private final StockService stockService;

    public enum SortField { updatedAt, availableQtt, minimumQtt, unitCost }

    @GetMapping
    @Operation(summary = "List stock items (paginated); belowMinimum=true lists items under the minimum")
    public Page<StockDTO> findAll(
            @RequestParam(required = false) UUID companyUnitId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean belowMinimum,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "updatedAt") SortField sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        return stockService.findAll(companyUnitId, search, belowMinimum,
                PageParams.of(page, size, direction, sortBy.name()));
    }

    @GetMapping("/{companyUnitId}/{partId}")
    @Operation(summary = "Find stock item by unit and part")
    public StockDTO findById(@PathVariable UUID companyUnitId, @PathVariable Long partId) {
        return stockService.findById(companyUnitId, partId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create stock item")
    public StockDTO create(@Valid @RequestBody StockCreateDTO dto) {
        return stockService.create(dto);
    }

    @PutMapping("/{companyUnitId}/{partId}")
    @Operation(summary = "Update stock item")
    public StockDTO update(@PathVariable UUID companyUnitId, @PathVariable Long partId,
                           @Valid @RequestBody StockUpdateDTO dto) {
        return stockService.update(companyUnitId, partId, dto);
    }

    @DeleteMapping("/{companyUnitId}/{partId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete stock item")
    public void delete(@PathVariable UUID companyUnitId, @PathVariable Long partId) {
        stockService.delete(companyUnitId, partId);
    }
}
