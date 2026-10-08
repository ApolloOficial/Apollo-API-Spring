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
@RequestMapping("/api/v1/panels")
@RequiredArgsConstructor
@Tag(name = "Panels", description = "Placas: consulta e acoes de campo do tecnico")
@SecurityRequirement(name = "bearer-key")
public class PanelController {

    private final PanelService panelService;

    public enum SortField { panelId, serialNumber, barcode, operatingStats, installationDt, stringCode }

    @GetMapping
    @Operation(summary = "List panels (paginated) with optional filters")
    public Page<PanelDTO> findAll(
            @RequestParam(required = false) UUID stringId,
            @RequestParam(required = false) UUID companyUnitId,
            @RequestParam(required = false) OperatingStatsEnum status,
            @RequestParam(required = false) String serialNumber,
            @RequestParam(required = false) String barcode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "panelId") SortField sortBy,
            @RequestParam(defaultValue = "ASC") Sort.Direction direction) {
        return panelService.findAll(stringId, companyUnitId, status, serialNumber, barcode,
                PageParams.of(page, size, direction, sortBy.name()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Find panel by ID")
    public PanelDTO findById(@PathVariable Long id) {
        return panelService.findById(id);
    }

    @GetMapping("/barcode/{barcode}")
    @Operation(summary = "Find panel by barcode (QR scan)")
    public PanelDTO findByBarcode(@PathVariable String barcode) {
        return panelService.findByBarcode(barcode);
    }

    @PostMapping("/activate")
    @Operation(summary = "Activate a panel from stock (pr_activate_panel)")
    public PanelDTO activate(@Valid @RequestBody PanelActivateDTO dto) {
        return panelService.activate(dto);
    }

    @PostMapping("/deactivate")
    @Operation(summary = "Deactivate a panel (pr_deactivate_panel)")
    public PanelDTO deactivate(@Valid @RequestBody PanelDeactivateDTO dto) {
        return panelService.deactivate(dto);
    }

    @PostMapping("/report-issue")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Technician reports a panel issue, creating a warning (pr_report_panel_issue)")
    public WarningDTO reportIssue(@Valid @RequestBody PanelIssueDTO dto) {
        return panelService.reportIssue(dto);
    }
}
