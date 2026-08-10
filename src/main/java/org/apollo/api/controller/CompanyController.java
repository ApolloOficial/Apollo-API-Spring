package org.apollo.api.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.CompanyDTO;
import org.apollo.api.service.CompanyService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
@Tag(name = "Company")
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping
    public List<CompanyDTO> findAll() {
        return companyService.findAll();
    }

    @GetMapping("/{id}")
    public CompanyDTO findById(@PathVariable Long id) {
        return companyService.findById(id);
    }

    @GetMapping("/name/{name}")
    public CompanyDTO findByName(@PathVariable String name) {
        return companyService.findByName(name);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompanyDTO create(@RequestBody CompanyDTO dto) {
        return companyService.create(dto);
    }

    @PutMapping("/{id}")
    public CompanyDTO update(@PathVariable Long id, @RequestBody CompanyDTO dto) {
        return companyService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        companyService.delete(id);
    }
}
