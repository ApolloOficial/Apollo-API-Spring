package org.apollo.api.controller;

import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.BatchDTO;
import org.apollo.api.service.BatchService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/batches")
@RequiredArgsConstructor
public class BatchController {

    private final BatchService batchService;

    @GetMapping
    public List<BatchDTO> findAll() {
        return batchService.findAll();
    }

    @GetMapping("/{id}")
    public BatchDTO findById(@PathVariable Long id) {
        return batchService.findById(id);
    }

    @PostMapping
    public BatchDTO create(@RequestBody BatchDTO dto) {
        return batchService.create(dto);
    }

    @PutMapping("/{id}")
    public BatchDTO update(@PathVariable Long id, @RequestBody BatchDTO dto) {
        return batchService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        batchService.delete(id);
    }
}