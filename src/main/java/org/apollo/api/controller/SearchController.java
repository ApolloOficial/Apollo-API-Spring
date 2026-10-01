package org.apollo.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.SearchResponseDTO;
import org.apollo.api.dto.SearchSuggestionResponseDTO;
import org.apollo.api.exception.ErrorResponse;
import org.apollo.api.security.TenantContext;
import org.apollo.api.service.SearchService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@Tag(name = "Search", description = "Cross-entity dynamic search operations")
@SecurityRequirement(name = "bearer-key")
public class SearchController {

    private final SearchService searchService;
    private final TenantContext tenantContext;

    @GetMapping
    @Operation(summary = "Search employees, company units, batches and relocations by free text")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Search results returned successfully"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid token",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\": 401, \"message\": \"Missing or invalid token\"}")))
    })
    public SearchResponseDTO search(@RequestParam String q) {
        return searchService.search(q, tenantContext.getCompanyId());
    }

    @GetMapping("/suggestions")
    @Operation(summary = "Get quick search suggestions (top 5 per category) for a partial query")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Suggestions returned successfully"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid token",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\": 401, \"message\": \"Missing or invalid token\"}")))
    })
    public SearchSuggestionResponseDTO suggestions(@RequestParam String q) {
        return searchService.suggestions(q, tenantContext.getCompanyId());
    }
}
