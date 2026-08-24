package org.apollo.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.SegmentDTO;
import org.apollo.api.service.SegmentService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/segments")
@RequiredArgsConstructor
@Tag(name = "Segment", description = "Segment management operations")
public class SegmentController {

    private final SegmentService segmentService;

    @GetMapping
    @Operation(summary = "List segments")
    @ApiResponse(responseCode = "200", description = "Segments returned successfully")
    public List<SegmentDTO> findAll() {
        return segmentService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Find segment by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Segment returned successfully"),
            @ApiResponse(responseCode = "404", description = "Segment not found")
    })
    public SegmentDTO findById(@PathVariable Long id) {
        return segmentService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create segment")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Segment created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid segment data")
    })
    public SegmentDTO create(@Valid @RequestBody SegmentDTO dto) {
        return segmentService.create(dto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update segment")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Segment updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid segment data"),
            @ApiResponse(responseCode = "404", description = "Segment not found")
    })
    public SegmentDTO update(@PathVariable Long id, @Valid @RequestBody SegmentDTO dto) {
        return segmentService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete segment")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Segment deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Segment not found")
    })
    public void delete(@PathVariable Long id) {
        segmentService.delete(id);
    }
}
