package org.apollo.api.repository;

import org.apollo.api.dto.SearchResultDTO;

import java.util.List;

public interface SearchRepository {

    List<SearchResultDTO> searchEmployees(String search, Long companyId, int limit);

    List<SearchResultDTO> searchCompanyUnits(String search, Long companyId, int limit);

    List<SearchResultDTO> searchBatches(String search, Long companyId, int limit);

    List<SearchResultDTO> searchInternalRelocations(String search, Long companyId, int limit);

    List<SearchResultDTO> searchExternalRelocations(String search, Long companyId, int limit);
}