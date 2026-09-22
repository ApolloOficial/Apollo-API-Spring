package org.apollo.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.WarningDTO;
import org.apollo.api.service.WarningService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/warnings")
@RequiredArgsConstructor
public class WarningController {

    private final WarningService warningService;

    @GetMapping
    public List<WarningDTO> findAll() { return warningService.findAll(); }

    @GetMapping("/{id}")
    public WarningDTO findById(@PathVariable Long id) { return warningService.findById(id); }

    @PostMapping
    public WarningDTO create(@Valid @RequestBody WarningDTO dto) { return warningService.create(dto); }

    @PatchMapping("/{id}/resolve")
    public WarningDTO resolve(@PathVariable Long id) { return warningService.resolve(id); }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { warningService.delete(id); }
}