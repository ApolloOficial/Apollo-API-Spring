package org.apollo.api.repository;

import org.apollo.api.dto.SearchResultDTO;
import org.apollo.api.model.Inverter;
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
import java.util.UUID;

public interface InverterRepository extends JpaRepository<Inverter, UUID>, JpaSpecificationExecutor<Inverter> {

    @Override
    @EntityGraph(attributePaths = {"companyUnit", "inverterModel"})
    Page<Inverter> findAll(Specification<Inverter> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"companyUnit", "inverterModel"})
    Optional<Inverter> findByIdAndCompanyUnitCompanyId(UUID id, Long companyId);

    @Query("""
            SELECT new org.apollo.api.dto.SearchResultDTO(i.id, i.code, 'Inverter')
            FROM Inverter i
            WHERE i.companyUnit.company.id = :companyId
              AND (LOWER(i.code) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(i.serialNumber) LIKE LOWER(CONCAT('%', :search, '%')))
            ORDER BY i.code ASC
            """)
    List<SearchResultDTO> search(@Param("search") String search, @Param("companyId") Long companyId, Pageable pageable);
}
