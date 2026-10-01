package org.apollo.api.service;

import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.SearchResponseDTO;
import org.apollo.api.dto.SearchResultDTO;
import org.apollo.api.dto.SearchSuggestionResponseDTO;
import org.apollo.api.repository.CompanyUnitRepository;
import org.apollo.api.repository.EmployeeRepository;
import org.apollo.api.repository.InverterRepository;
import org.apollo.api.repository.PanelRepository;
import org.apollo.api.repository.SuggestedExternalRelocationRepository;
import org.apollo.api.repository.SuggestedInternalRelocationRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

// Busca global por texto livre: funcionarios, filiais, inversores, placas e realocacoes.
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchService {

    private static final int SEARCH_LIMIT = 10;
    private static final int SUGGESTION_LIMIT = 5;

    private final EmployeeRepository employeeRepository;
    private final CompanyUnitRepository companyUnitRepository;
    private final InverterRepository inverterRepository;
    private final PanelRepository panelRepository;
    private final SuggestedInternalRelocationRepository internalRelocationRepository;
    private final SuggestedExternalRelocationRepository externalRelocationRepository;

    public SearchResponseDTO search(String query, Long companyId) {
        String search = normalize(query);
        if (search.isBlank()) {
            return new SearchResponseDTO(search, List.of(), List.of(), List.of(), List.of(), List.of());
        }
        Pageable limit = PageRequest.of(0, SEARCH_LIMIT);
        return new SearchResponseDTO(
                search,
                employeeRepository.search(search, companyId, limit),
                companyUnitRepository.search(search, companyId, limit),
                inverterRepository.search(search, companyId, limit),
                panelRepository.search(search, companyId, limit),
                getRelocations(search, companyId, SEARCH_LIMIT));
    }

    public SearchSuggestionResponseDTO suggestions(String query, Long companyId) {
        String search = normalize(query);
        if (search.isBlank()) {
            return new SearchSuggestionResponseDTO(List.of(), List.of(), List.of(), List.of(), List.of());
        }
        Pageable limit = PageRequest.of(0, SUGGESTION_LIMIT);
        return new SearchSuggestionResponseDTO(
                employeeRepository.search(search, companyId, limit),
                companyUnitRepository.search(search, companyId, limit),
                inverterRepository.search(search, companyId, limit),
                panelRepository.search(search, companyId, limit),
                getRelocations(search, companyId, SUGGESTION_LIMIT));
    }

    private List<SearchResultDTO> getRelocations(String search, Long companyId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<SearchResultDTO> relocations = new ArrayList<>();
        relocations.addAll(internalRelocationRepository.search(search, companyId, pageable));
        relocations.addAll(externalRelocationRepository.search(search, companyId, pageable));
        return relocations.stream().limit(limit).toList();
    }

    private String normalize(String query) {
        return query == null ? "" : query.trim();
    }
}
