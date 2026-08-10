package org.apollo.api.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.RolesDTO;
import org.apollo.api.service.RolesService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@Tag(name = "Roles")
public class RolesController {

    private final RolesService rolesService;

    @GetMapping
    public List<RolesDTO> findAll() {
        return rolesService.findAll();
    }

    @GetMapping("/{id}")
    public RolesDTO findById(@PathVariable Long id) {
        return rolesService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RolesDTO create(@RequestBody RolesDTO dto) {
        return rolesService.create(dto);
    }

    @PutMapping("/{id}")
    public RolesDTO update(@PathVariable Long id, @RequestBody RolesDTO dto) {
        return rolesService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        rolesService.delete(id);
    }
}
