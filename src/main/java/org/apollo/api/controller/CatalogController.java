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
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Catalog", description = "Catalogos de referencia (pecas e modelos) - somente leitura")
@SecurityRequirement(name = "bearer-key")
public class CatalogController {

    private final CatalogService catalogService;

    @GetMapping("/parts")
    @Operation(summary = "List parts (paginated)")
    public Page<PartDTO> parts(@RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "50") int size) {
        return catalogService.parts(PageParams.of(page, size, Sort.Direction.ASC, "name"));
    }

    @GetMapping("/panel-models")
    @Operation(summary = "List panel models (paginated)")
    public Page<PanelModelDTO> panelModels(@RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "50") int size) {
        return catalogService.panelModels(PageParams.of(page, size, Sort.Direction.ASC, "manufacturer"));
    }

    @GetMapping("/inverter-models")
    @Operation(summary = "List inverter models (paginated)")
    public Page<InverterModelDTO> inverterModels(@RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "50") int size) {
        return catalogService.inverterModels(PageParams.of(page, size, Sort.Direction.ASC, "brand"));
    }
}
