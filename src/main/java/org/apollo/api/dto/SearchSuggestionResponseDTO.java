package org.apollo.api.dto;

import java.util.List;

public record SearchSuggestionResponseDTO(List<SearchResultDTO> employees, List<SearchResultDTO> companyUnits,
                                          List<SearchResultDTO> inverters, List<SearchResultDTO> panels,
                                          List<SearchResultDTO> relocations) {
}
