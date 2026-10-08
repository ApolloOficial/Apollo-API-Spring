package org.apollo.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.SegmentDTO;
import org.apollo.api.service.SegmentService;
import org.apollo.api.util.PageParams;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/segments")
@RequiredArgsConstructor
@Tag(name = "Segment", description = "Segmentos de mercado - somente leitura")
@SecurityRequirement(name = "bearer-key")
public class SegmentController {

    private final SegmentService segmentService;

    @GetMapping
    @Operation(summary = "List segments (paginated)")
    public Page<SegmentDTO> findAll(@RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "20") int size) {
        return segmentService.findAll(PageParams.of(page, size, Sort.Direction.ASC, "id"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Find segment by ID")
    public SegmentDTO findById(@PathVariable Long id) {
        return segmentService.findById(id);
    }
}
