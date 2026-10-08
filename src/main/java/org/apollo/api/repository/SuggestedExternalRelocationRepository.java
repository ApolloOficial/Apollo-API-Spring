package org.apollo.api.repository;

import org.apollo.api.dto.SearchResultDTO;
import org.apollo.api.model.SuggestedExternalRelocation;
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

public interface SuggestedExternalRelocationRepository
        extends JpaRepository<SuggestedExternalRelocation, Long>, JpaSpecificationExecutor<SuggestedExternalRelocation> {

    @Override
    @EntityGraph(attributePaths = {"panel", "panel.panelString", "panel.panelString.inverter",
            "panel.panelString.inverter.companyUnit", "destinationCompany", "segment", "requestedBy", "reviewedBy"})
    Page<SuggestedExternalRelocation> findAll(Specification<SuggestedExternalRelocation> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"panel", "panel.panelString", "panel.panelString.inverter",
            "panel.panelString.inverter.companyUnit", "destinationCompany", "segment", "requestedBy", "reviewedBy"})
    Optional<SuggestedExternalRelocation> findByIdAndPanelPanelStringInverterCompanyUnitCompanyId(Long id, Long companyId);

    @Query("""
            SELECT new org.apollo.api.dto.SearchResultDTO(r.id, r.justification, 'External Relocation')
            FROM SuggestedExternalRelocation r
            WHERE r.panel.panelString.inverter.companyUnit.company.id = :companyId
              AND LOWER(r.justification) LIKE LOWER(CONCAT('%', :search, '%'))
            ORDER BY r.suggestedAt DESC
            """)
    List<SearchResultDTO> search(@Param("search") String search, @Param("companyId") Long companyId, Pageable pageable);
}
