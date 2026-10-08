package org.apollo.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apollo.api.exception.ErrorResponse;
import org.apollo.api.dto.CompanyUnitDTO;
import org.apollo.api.service.CompanyUnitService;
import org.apollo.api.util.PageParams;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/company-units")
@RequiredArgsConstructor
@Tag(name = "Company Unit", description = "Company unit management operations")
@SecurityRequirement(name = "bearer-key")
public class CompanyUnitController {

    private final CompanyUnitService companyUnitService;

    /** Campos permitidos para ordenar a listagem. */
    public enum SortField { name, createdAt, kwpTotal }

    @GetMapping
    @Operation(summary = "List company units")
    @ApiResponse(responseCode = "200", description = "Company units returned successfully")
    public Page<CompanyUnitDTO> findAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Long segmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") SortField sortBy,
            @RequestParam(defaultValue = "ASC") Sort.Direction direction) {
        return companyUnitService.findAll(name, city, state, active, segmentId,
                PageParams.of(page, size, direction, sortBy.name()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Find company unit by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Company unit returned successfully"),
            @ApiResponse(responseCode = "404", description = "Company unit not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\": 404, \"message\": \"Unit not found: 1\"}")))
    })
    public CompanyUnitDTO findById(@PathVariable UUID id) {
        return companyUnitService.findById(id);
    }

    @GetMapping("/segment/{segmentId}")
    @Operation(summary = "List company units by segment")
    @ApiResponse(responseCode = "200", description = "Company units returned successfully")
    public List<CompanyUnitDTO> findBySegmentId(@PathVariable Long segmentId) {
        return companyUnitService.findBySegmentId(segmentId);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update contact data, address and responsible of a company unit")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Company unit updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid company unit data",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\": 400, \"message\": \"name: Name is required\"}"))),
            @ApiResponse(responseCode = "404", description = "Company unit or segment not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\": 404, \"message\": \"Unit not found: 1\"}")))
    })
    public CompanyUnitDTO update(@PathVariable UUID id, @Valid @RequestBody CompanyUnitDTO dto) {
        return companyUnitService.update(id, dto);
    }
}
