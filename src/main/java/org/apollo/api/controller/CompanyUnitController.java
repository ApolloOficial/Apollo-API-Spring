package org.apollo.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.CompanyUnitDTO;
import org.apollo.api.service.CompanyUnitService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/company-units")
@RequiredArgsConstructor
@Tag(name = "Company Unit", description = "Company unit management operations")
@SecurityRequirement(name = "bearer-key")
public class CompanyUnitController {

    private final CompanyUnitService companyUnitService;

    @GetMapping
    @Operation(summary = "List company units")
    @ApiResponse(responseCode = "200", description = "Company units returned successfully")
    public List<CompanyUnitDTO> findAll() {
        return companyUnitService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Find company unit by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Company unit returned successfully"),
            @ApiResponse(responseCode = "404", description = "Company unit not found")
    })
    public CompanyUnitDTO findById(@PathVariable Long id) {
        return companyUnitService.findById(id);
    }

    @GetMapping("/segment/{segmentId}")
    @Operation(summary = "List company units by segment")
    @ApiResponse(responseCode = "200", description = "Company units returned successfully")
    public List<CompanyUnitDTO> findBySegmentId(@PathVariable Long segmentId) {
        return companyUnitService.findBySegmentId(segmentId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create company unit")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Company unit created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid company unit data"),
            @ApiResponse(responseCode = "404", description = "Segment not found")
    })
    public CompanyUnitDTO create(@Valid @RequestBody CompanyUnitDTO dto) {
        return companyUnitService.create(dto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update company unit")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Company unit updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid company unit data"),
            @ApiResponse(responseCode = "404", description = "Company unit or segment not found")
    })
    public CompanyUnitDTO update(@PathVariable Long id, @Valid @RequestBody CompanyUnitDTO dto) {
        return companyUnitService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete company unit")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Company unit deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Company unit not found")
    })
    public void delete(@PathVariable Long id) {
        companyUnitService.delete(id);
    }
}
