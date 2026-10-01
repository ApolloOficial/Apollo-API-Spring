package org.apollo.api.repository;

import org.apollo.api.dto.SearchResultDTO;
import org.apollo.api.model.Panel;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PanelRepository extends JpaRepository<Panel, Long> {

    @EntityGraph(attributePaths = {"panelString", "panelString.inverter", "panelString.inverter.companyUnit"})
    Optional<Panel> findByIdAndPanelStringInverterCompanyUnitCompanyId(Long id, Long companyId);

    @EntityGraph(attributePaths = {"panelString", "panelString.inverter", "panelString.inverter.companyUnit"})
    Optional<Panel> findByBarcodeAndPanelStringInverterCompanyUnitCompanyId(String barcode, Long companyId);

    @Query("""
            SELECT new org.apollo.api.dto.SearchResultDTO(p.id, p.serialNumber, 'Panel')
            FROM Panel p
            WHERE p.panelString.inverter.companyUnit.company.id = :companyId
              AND (LOWER(p.serialNumber) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(p.barcode) LIKE LOWER(CONCAT('%', :search, '%')))
            ORDER BY p.serialNumber ASC
            """)
    List<SearchResultDTO> search(@Param("search") String search, @Param("companyId") Long companyId, Pageable pageable);
}
