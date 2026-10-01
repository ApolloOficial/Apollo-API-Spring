package org.apollo.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.RolesDTO;
import org.apollo.api.service.RolesService;
import org.apollo.api.util.PageParams;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@Tag(name = "Roles", description = "Cargos (GERENTE, ANALISTA, OPERADOR, TECNICO) - somente leitura")
@SecurityRequirement(name = "bearer-key")
public class RolesController {

    private final RolesService rolesService;

    @GetMapping
    @Operation(summary = "List roles (paginated)")
    public Page<RolesDTO> findAll(@RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "20") int size) {
        return rolesService.findAll(PageParams.of(page, size, Sort.Direction.ASC, "id"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Find role by ID")
    public RolesDTO findById(@PathVariable Long id) {
        return rolesService.findById(id);
    }
}
