package org.apollo.api.controller;

import jakarta.validation.Valid;
import org.apollo.api.dto.EmployeeDTO;
import org.apollo.api.service.EmployeeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public ResponseEntity<List<EmployeeDTO>> findAll() {
        return ResponseEntity.ok(employeeService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(employeeService.findById(id));
    }

    @PostMapping
    public ResponseEntity<EmployeeDTO> create(@Valid @RequestBody EmployeeDTO dto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(employeeService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDTO> update(@PathVariable UUID id, @Valid @RequestBody EmployeeDTO dto) {
        return ResponseEntity.ok(
                employeeService.update(id, dto)
        );
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<EmployeeDTO> deactivate(@PathVariable UUID id) {
        return ResponseEntity.ok(
                employeeService.deactivate(id)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        employeeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}