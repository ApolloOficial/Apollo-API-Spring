package org.apollo.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.CompanyDTO;
import org.apollo.api.service.CompanyService;
import org.apollo.api.util.PageParams;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
@Tag(name = "Company", description = "Empresa do usuario logado - somente leitura")
@SecurityRequirement(name = "bearer-key")
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping
    @Operation(summary = "List companies (the user only sees its own)")
    public Page<CompanyDTO> findAll(@RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "20") int size) {
        return companyService.findAll(PageParams.of(page, size, Sort.Direction.ASC, "id"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Find company by ID")
    public CompanyDTO findById(@PathVariable Long id) {
        return companyService.findById(id);
    }

    @GetMapping("/name/{name}")
    @Operation(summary = "Find company by name")
    public CompanyDTO findByName(@PathVariable String name) {
        return companyService.findByName(name);
    }
}
