package org.apollo.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.MaintenanceRegisterDTO;
import org.apollo.api.service.MaintenanceRegisterService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/maintenance-registers")
@RequiredArgsConstructor
public class MaintenanceRegisterController {

    private final MaintenanceRegisterService maintenanceRegisterService;

    @GetMapping
    public List<MaintenanceRegisterDTO> findAll() {
        return maintenanceRegisterService.findAll();
    }

    @GetMapping("/{id}")
    public MaintenanceRegisterDTO findById(@PathVariable Long id) {
        return maintenanceRegisterService.findById(id);
    }

    @PostMapping
    public MaintenanceRegisterDTO create(@Valid @RequestBody MaintenanceRegisterDTO dto) {
        return maintenanceRegisterService.create(dto);
    }

    @PutMapping("/{id}")
    public MaintenanceRegisterDTO update(@PathVariable Long id, @Valid @RequestBody MaintenanceRegisterDTO dto) {
        return maintenanceRegisterService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        maintenanceRegisterService.delete(id);
    }
}