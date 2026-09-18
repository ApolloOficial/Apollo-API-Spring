package org.apollo.api.dto;

import java.util.List;

public class SearchResponseDTO {

    private String query;

    private List<SearchResultDTO> employees;
    private List<SearchResultDTO> companyUnits;
    private List<SearchResultDTO> batches;
    private List<SearchResultDTO> relocations;

    public SearchResponseDTO() {
    }

    public SearchResponseDTO(
            String query,
            List<SearchResultDTO> employees,
            List<SearchResultDTO> companyUnits,
            List<SearchResultDTO> batches,
            List<SearchResultDTO> relocations
    ) {
        this.query = query;
        this.employees = employees;
        this.companyUnits = companyUnits;
        this.batches = batches;
        this.relocations = relocations;
    }

    public String getQuery() {
        return query;
    }

    public List<SearchResultDTO> getEmployees() {
        return employees;
    }

    public List<SearchResultDTO> getCompanyUnits() {
        return companyUnits;
    }

    public List<SearchResultDTO> getBatches() {
        return batches;
    }

    public List<SearchResultDTO> getRelocations() {
        return relocations;
    }
}