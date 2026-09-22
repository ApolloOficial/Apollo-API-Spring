package org.apollo.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.SuggestedExternalRelocationDTO;
import org.apollo.api.enums.RelocationStatusEnum;
import org.apollo.api.service.SuggestedExternalRelocationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/relocations/external")
@RequiredArgsConstructor
public class SuggestedExternalRelocationController {

    private final SuggestedExternalRelocationService relocationService;

    @GetMapping
    public List<SuggestedExternalRelocationDTO> findAll() { return relocationService.findAll(); }

    @PostMapping
    public SuggestedExternalRelocationDTO create(@Valid @RequestBody SuggestedExternalRelocationDTO dto) {
        return relocationService.create(dto);
    }

    @PatchMapping("/{id}/review")
    public SuggestedExternalRelocationDTO review(@PathVariable Long id, @RequestParam RelocationStatusEnum status) {
        return relocationService.review(id, status);
    }
}