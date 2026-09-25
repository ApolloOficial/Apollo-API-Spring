package org.apollo.api.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.SearchResponseDTO;
import org.apollo.api.dto.SearchSuggestionResponseDTO;
import org.apollo.api.security.TenantContext;
import org.apollo.api.service.SearchService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-key")
public class SearchController {

    private final org.apollo.api.service.SearchService searchService;
    private final TenantContext tenantContext;

    @GetMapping
    public SearchResponseDTO search(@RequestParam String q) {
        return searchService.search(q, tenantContext.getCompanyId());
    }

    @GetMapping("/suggestions")
    public SearchSuggestionResponseDTO suggestions(@RequestParam String q) {
        return searchService.suggestions(q, tenantContext.getCompanyId());
    }
}