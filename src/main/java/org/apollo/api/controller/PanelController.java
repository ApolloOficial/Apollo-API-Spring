package org.apollo.api.controller;

import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.PanelDTO;
import org.apollo.api.service.PanelService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/panels")
@RequiredArgsConstructor
public class PanelController {

    private final PanelService panelService;

    @GetMapping
    public List<PanelDTO> findAll() {
        return panelService.findAll();
    }

    @GetMapping("/{id}")
    public PanelDTO findById(@PathVariable Long id) {
        return panelService.findById(id);
    }

    @PostMapping
    public PanelDTO create(@RequestBody PanelDTO dto) {
        return panelService.create(dto);
    }

    @PutMapping("/{id}")
    public PanelDTO update(@PathVariable Long id, @RequestBody PanelDTO dto) {
        return panelService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        panelService.delete(id);
    }
}