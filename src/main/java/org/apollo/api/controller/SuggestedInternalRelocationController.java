package org.apollo.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.SuggestedInternalRelocationDTO;
import org.apollo.api.enums.RelocationStatusEnum;
import org.apollo.api.service.SuggestedInternalRelocationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/relocations/internal")
@RequiredArgsConstructor
public class SuggestedInternalRelocationController {

    private final SuggestedInternalRelocationService relocationService;

    @GetMapping
    public List<SuggestedInternalRelocationDTO> findAll() { return relocationService.findAll(); }

    @PostMapping
    public SuggestedInternalRelocationDTO create(@Valid @RequestBody SuggestedInternalRelocationDTO dto) {
        return relocationService.create(dto);
    }

    @PatchMapping("/{id}/review")
    public SuggestedInternalRelocationDTO review(@PathVariable Long id,
                                                 @RequestParam RelocationStatusEnum status,
                                                 @RequestParam UUID reviewerId) {
        return relocationService.review(id, status, reviewerId);
    }
}