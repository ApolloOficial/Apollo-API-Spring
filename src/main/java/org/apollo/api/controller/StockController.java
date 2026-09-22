package org.apollo.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.StockDTO;
import org.apollo.api.service.StockService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @GetMapping
    public List<StockDTO> findAll() { return stockService.findAll(); }

    @GetMapping("/{id}")
    public StockDTO findById(@PathVariable Long id) { return stockService.findById(id); }

    @PostMapping
    public StockDTO create(@Valid @RequestBody StockDTO dto) { return stockService.create(dto); }

    @PutMapping("/{id}")
    public StockDTO update(@PathVariable Long id, @Valid @RequestBody StockDTO dto) { return stockService.update(id, dto); }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { stockService.delete(id); }
}