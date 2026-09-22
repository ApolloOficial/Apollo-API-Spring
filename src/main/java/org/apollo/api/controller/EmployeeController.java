package org.apollo.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.EmployeeCreateDTO;
import org.apollo.api.dto.EmployeeDTO;
import org.apollo.api.service.EmployeeService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
@Tag(name = "Employees", description = "Employee management operations")
@SecurityRequirement(name = "bearer-key")
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    public List<EmployeeDTO> findAll() {
        return employeeService.findAll();
    }

    @GetMapping("/{id}")
    public EmployeeDTO findById(@PathVariable UUID id) {
        return employeeService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create employee")
    public EmployeeDTO create(@Valid @RequestBody EmployeeCreateDTO dto) {
        return employeeService.create(dto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update employee")
    public EmployeeDTO update(@PathVariable UUID id, @Valid @RequestBody EmployeeDTO dto) {
        return employeeService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete employee")
    public void delete(@PathVariable UUID id) {
        employeeService.delete(id);
    }
}
