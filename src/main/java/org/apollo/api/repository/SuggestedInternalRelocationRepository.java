package org.apollo.api.repository;

import org.apollo.api.dto.SearchResultDTO;
import org.apollo.api.model.SuggestedInternalRelocation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SuggestedInternalRelocationRepository
        extends JpaRepository<SuggestedInternalRelocation, Long>, JpaSpecificationExecutor<SuggestedInternalRelocation> {

    @Override
    @EntityGraph(attributePaths = {"panelString", "panelString.inverter", "panelString.inverter.companyUnit",
            "suggestedUnit", "requestedBy", "reviewedBy"})
    Page<SuggestedInternalRelocation> findAll(Specification<SuggestedInternalRelocation> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"panelString", "panelString.inverter", "panelString.inverter.companyUnit",
            "suggestedUnit", "requestedBy", "reviewedBy"})
    Optional<SuggestedInternalRelocation> findByIdAndPanelStringInverterCompanyUnitCompanyId(Long id, Long companyId);

    @Query("""
            SELECT new org.apollo.api.dto.SearchResultDTO(r.id, r.justification, 'Internal Relocation')
            FROM SuggestedInternalRelocation r
            WHERE r.panelString.inverter.companyUnit.company.id = :companyId
              AND LOWER(r.justification) LIKE LOWER(CONCAT('%', :search, '%'))
            ORDER BY r.suggestedAt DESC
            """)
    List<SearchResultDTO> search(@Param("search") String search, @Param("companyId") Long companyId, Pageable pageable);
}
