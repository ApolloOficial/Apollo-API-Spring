package org.apollo.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.BatchDTO;
import org.apollo.api.service.BatchService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/batches")
@RequiredArgsConstructor
@Tag(name = "Batch", description = "Batch management operations")
public class BatchController {

    private final BatchService batchService;

    @GetMapping
    @Operation(summary = "List batches")
    @ApiResponse(responseCode = "200", description = "Batches returned successfully")
    public List<BatchDTO> findAll() {
        return batchService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Find batch by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Batch returned successfully"),
            @ApiResponse(responseCode = "404", description = "Batch not found")
    })
    public BatchDTO findById(@PathVariable Long id) {
        return batchService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Create batch")
    @ApiResponse(responseCode = "200", description = "Batch created successfully")
    public BatchDTO create(@Valid @RequestBody BatchDTO dto) {
        return batchService.create(dto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update batch")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Batch updated successfully"),
            @ApiResponse(responseCode = "404", description = "Batch not found")
    })
    public BatchDTO update(@PathVariable Long id, @Valid @RequestBody BatchDTO dto) {
        return batchService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete batch")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Batch deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Batch not found")
    })
    public void delete(@PathVariable Long id) {
        batchService.delete(id);
    }
}
