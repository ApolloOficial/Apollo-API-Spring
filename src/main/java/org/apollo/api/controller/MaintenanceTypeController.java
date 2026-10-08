package org.apollo.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.MaintenanceTypeDTO;
import org.apollo.api.service.MaintenanceTypeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/maintenance-types")
@RequiredArgsConstructor
@Tag(name = "Maintenance Types", description = "Maintenance types (read only)")
@SecurityRequirement(name = "bearer-key")
public class MaintenanceTypeController {

    private final MaintenanceTypeService maintenanceTypeService;

    @GetMapping
    @Operation(summary = "List maintenance types (small fixed list, not paginated)")
    public List<MaintenanceTypeDTO> findAll() {
        return maintenanceTypeService.findAll();
    }
}
