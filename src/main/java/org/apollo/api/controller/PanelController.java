package org.apollo.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.PanelDTO;
import org.apollo.api.service.PanelService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/panels")
@RequiredArgsConstructor
@Tag(name = "Panel", description = "Panel management operations")
public class PanelController {

    private final PanelService panelService;

    @GetMapping
    @Operation(summary = "List panels")
    @ApiResponse(responseCode = "200", description = "Panels returned successfully")
    public List<PanelDTO> findAll() {
        return panelService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Find panel by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Panel returned successfully"),
            @ApiResponse(responseCode = "404", description = "Panel not found")
    })
    public PanelDTO findById(@PathVariable Long id) {
        return panelService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Create panel")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Panel created successfully"),
            @ApiResponse(responseCode = "404", description = "Batch not found")
    })
    public PanelDTO create(@Valid @RequestBody PanelDTO dto) {
        return panelService.create(dto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update panel")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Panel updated successfully"),
            @ApiResponse(responseCode = "404", description = "Panel or batch not found")
    })
    public PanelDTO update(@PathVariable Long id, @Valid @RequestBody PanelDTO dto) {
        return panelService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete panel")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Panel deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Panel not found")
    })
    public void delete(@PathVariable Long id) {
        panelService.delete(id);
    }
}
