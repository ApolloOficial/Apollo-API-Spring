package org.apollo.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apollo.api.exception.ErrorResponse;
import org.apollo.api.dto.CompanyDTO;
import org.apollo.api.service.CompanyService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
@Tag(name = "Company", description = "Company management operations")
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping
    @Operation(summary = "List companies")
    @ApiResponse(responseCode = "200", description = "Companies returned successfully")
    public List<CompanyDTO> findAll() {
        return companyService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Find company by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Company returned successfully"),
            @ApiResponse(responseCode = "404", description = "Company not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\": 404, \"message\": \"Empresa não encontrada: 1\"}")))
    })
    public CompanyDTO findById(@PathVariable Long id) {
        return companyService.findById(id);
    }

    @GetMapping("/name/{name}")
    @Operation(summary = "Find company by name")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Company returned successfully"),
            @ApiResponse(responseCode = "404", description = "Company not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\": 404, \"message\": \"Empresa não encontrada: 1\"}")))
    })
    public CompanyDTO findByName(@PathVariable String name) {
        return companyService.findByName(name);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create company")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Company created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid company data",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\": 400, \"message\": \"name: Nome é obrigatório\"}")))
    })
    public CompanyDTO create(@Valid @RequestBody CompanyDTO dto) {
        return companyService.create(dto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update company")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Company updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid company data",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\": 400, \"message\": \"name: Nome é obrigatório\"}"))),
            @ApiResponse(responseCode = "404", description = "Company not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\": 404, \"message\": \"Empresa não encontrada: 1\"}")))
    })
    public CompanyDTO update(@PathVariable Long id, @Valid @RequestBody CompanyDTO dto) {
        return companyService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete company")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Company deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Company not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\": 404, \"message\": \"Empresa não encontrada: 1\"}")))
    })
    public void delete(@PathVariable Long id) {
        companyService.delete(id);
    }
}
