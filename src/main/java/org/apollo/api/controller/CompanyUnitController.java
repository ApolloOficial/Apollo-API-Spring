package org.apollo.api.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.CompanyUnitDTO;
import org.apollo.api.service.CompanyUnitService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/company-units")
@RequiredArgsConstructor
@Tag(name = "Company Unit")
public class CompanyUnitController {

    private final CompanyUnitService companyUnitService;

    @GetMapping
    public List<CompanyUnitDTO> findAll() {
        return companyUnitService.findAll();
    }

    @GetMapping("/{id}")
    public CompanyUnitDTO findById(@PathVariable Long id) {
        return companyUnitService.findById(id);
    }

    @GetMapping("/segment/{segmentId}")
    public List<CompanyUnitDTO> findBySegmentId(@PathVariable Long segmentId) {
        return companyUnitService.findBySegmentId(segmentId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompanyUnitDTO create(@Valid @RequestBody CompanyUnitDTO dto) {
        return companyUnitService.create(dto);
    }

    @PutMapping("/{id}")
    public CompanyUnitDTO update(@PathVariable Long id, @Valid @RequestBody CompanyUnitDTO dto) {
        return companyUnitService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        companyUnitService.delete(id);
    }
}
