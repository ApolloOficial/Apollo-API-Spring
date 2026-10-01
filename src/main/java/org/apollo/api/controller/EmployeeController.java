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
import org.apollo.api.dto.EmployeeCreateDTO;
import org.apollo.api.dto.EmployeeDTO;
import org.apollo.api.dto.EmployeeUpdateDTO;
import org.apollo.api.exception.ErrorResponse;
import org.apollo.api.service.EmployeeService;
import org.springframework.data.domain.Page;
import org.apollo.api.util.PageParams;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
@Tag(name = "Employees", description = "Employee management operations")
@SecurityRequirement(name = "bearer-key")
public class EmployeeController {

    private final EmployeeService employeeService;

    /** Campos pelos quais a listagem pode ser ordenada (lista fechada: nao ha erro de digitacao). */
    public enum SortField { fullName, email, createdAt, isActive }

    // Listagem geral paginada. TODOS os filtros sao opcionais; sem nenhum, lista todos
    // os funcionarios da empresa do usuario logado. fullName e email fazem busca parcial
    // (LIKE, sem diferenciar maiusculas). role aceita o ID ou o nome do cargo.
    @GetMapping
    @Operation(summary = "List employees (paginated), optionally filtered by name, email, role and active status")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employees returned successfully"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid token",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\": 401, \"message\": \"Missing or invalid token\"}")))
    })
    public Page<EmployeeDTO> findAll(
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) UUID companyUnitId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "fullName") SortField sortBy,
            @RequestParam(defaultValue = "ASC") Sort.Direction direction
    ) {
        return employeeService.findAll(fullName, email, role, isActive, companyUnitId,
                PageParams.of(page, size, direction, sortBy.name()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Find employee by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employee returned successfully"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid token",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\": 401, \"message\": \"Missing or invalid token\"}"))),
            @ApiResponse(responseCode = "404", description = "Employee not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\": 404, \"message\": \"Employee not found: 1\"}")))
    })
    public EmployeeDTO findById(@PathVariable UUID id) {
        return employeeService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create employee")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Employee created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid employee data"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid token",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\": 401, \"message\": \"Missing or invalid token\"}")))
    })
    public EmployeeDTO create(@Valid @RequestBody EmployeeCreateDTO dto) {
        return employeeService.create(dto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update employee")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employee updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid employee data"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid token",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\": 401, \"message\": \"Missing or invalid token\"}"))),
            @ApiResponse(responseCode = "404", description = "Employee not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\": 404, \"message\": \"Employee not found: 1\"}")))
    })
    public EmployeeDTO update(@PathVariable UUID id, @Valid @RequestBody EmployeeUpdateDTO dto) {
        return employeeService.update(id, dto);
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate employee (soft delete)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employee deactivated successfully"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid token",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\": 401, \"message\": \"Missing or invalid token\"}"))),
            @ApiResponse(responseCode = "404", description = "Employee not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\": 404, \"message\": \"Employee not found: 1\"}")))
    })
    public EmployeeDTO deactivate(@PathVariable UUID id) {
        return employeeService.deactivate(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Permanently delete employee")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Employee deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid token",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\": 401, \"message\": \"Missing or invalid token\"}"))),
            @ApiResponse(responseCode = "404", description = "Employee not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\": 404, \"message\": \"Employee not found: 1\"}")))
    })
    public void delete(@PathVariable UUID id) {
        employeeService.delete(id);
    }
}
