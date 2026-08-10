package org.apollo.api.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.SegmentDTO;
import org.apollo.api.service.SegmentService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/segments")
@RequiredArgsConstructor
@Tag(name = "Segment")
public class SegmentController {

    private final SegmentService segmentService;

    @GetMapping
    public List<SegmentDTO> findAll() {
        return segmentService.findAll();
    }

    @GetMapping("/{id}")
    public SegmentDTO findById(@PathVariable Long id) {
        return segmentService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SegmentDTO create(@RequestBody SegmentDTO dto) {
        return segmentService.create(dto);
    }

    @PutMapping("/{id}")
    public SegmentDTO update(@PathVariable Long id, @RequestBody SegmentDTO dto) {
        return segmentService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        segmentService.delete(id);
    }
}
