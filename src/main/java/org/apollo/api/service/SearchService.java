package org.apollo.api.service;

import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.SearchResponseDTO;
import org.apollo.api.dto.SearchResultDTO;
import org.apollo.api.dto.SearchSuggestionResponseDTO;
import org.apollo.api.repository.SearchRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {

    private static final int SEARCH_LIMIT = 10;
    private static final int SUGGESTION_LIMIT = 5;

    private final SearchRepository searchRepository;

    public SearchResponseDTO search(String query, Long companyId) {

        String search = normalize(query);

        if (search.isBlank()) {
            return new SearchResponseDTO(
                    search,
                    List.of(),
                    List.of(),
                    List.of(),
                    List.of()
            );
        }

        List<SearchResultDTO> employees =
                searchRepository.searchEmployees(
                        search,
                        companyId,
                        SEARCH_LIMIT
                );

        List<SearchResultDTO> companyUnits =
                searchRepository.searchCompanyUnits(
                        search,
                        companyId,
                        SEARCH_LIMIT
                );

        List<SearchResultDTO> batches =
                searchRepository.searchBatches(
                        search,
                        companyId,
                        SEARCH_LIMIT
                );

        List<SearchResultDTO> relocations =
                getRelocations(search, companyId, SEARCH_LIMIT);

        return new SearchResponseDTO(
                search,
                employees,
                companyUnits,
                batches,
                relocations
        );
    }

    public SearchSuggestionResponseDTO suggestions(
            String query,
            Long companyId
    ) {

        String search = normalize(query);

        if (search.isBlank()) {
            return new SearchSuggestionResponseDTO(
                    List.of(),
                    List.of(),
                    List.of(),
                    List.of()
            );
        }

        List<SearchResultDTO> employees =
                searchRepository.searchEmployees(
                        search,
                        companyId,
                        SUGGESTION_LIMIT
                );

        List<SearchResultDTO> companyUnits =
                searchRepository.searchCompanyUnits(
                        search,
                        companyId,
                        SUGGESTION_LIMIT
                );

        List<SearchResultDTO> batches =
                searchRepository.searchBatches(
                        search,
                        companyId,
                        SUGGESTION_LIMIT
                );

        List<SearchResultDTO> relocations =
                getRelocations(search, companyId, SUGGESTION_LIMIT);

        return new SearchSuggestionResponseDTO(
                employees,
                companyUnits,
                batches,
                relocations
        );
    }

    private List<SearchResultDTO> getRelocations(
            String search,
            Long companyId,
            int limit
    ) {

        List<SearchResultDTO> relocations = new ArrayList<>();

        relocations.addAll(
                searchRepository.searchInternalRelocations(
                        search,
                        companyId,
                        limit
                )
        );

        relocations.addAll(
                searchRepository.searchExternalRelocations(
                        search,
                        companyId,
                        limit
                )
        );

        return relocations.stream()
                .limit(limit)
                .toList();
    }

    private String normalize(String query) {

        if (query == null) {
            return "";
        }

        return query.trim();
    }
}