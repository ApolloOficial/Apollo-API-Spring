package org.apollo.api.repository;

import org.apollo.api.model.CompanyUnit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CompanyUnitRepository extends JpaRepository<CompanyUnit, UUID>,
        org.springframework.data.jpa.repository.JpaSpecificationExecutor<CompanyUnit> {

    @Override
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"address", "segment", "responsibleEmployee"})
    org.springframework.data.domain.Page<CompanyUnit> findAll(
            org.springframework.data.jpa.domain.Specification<CompanyUnit> spec,
            org.springframework.data.domain.Pageable pageable);

    Optional<CompanyUnit> findByIdAndCompanyId(UUID id, Long companyId);
    List<CompanyUnit> findBySegmentIdAndCompanyId(Long segmentId, Long companyId);
    Optional<CompanyUnit> findByAddressId(Long addressId);

    @org.springframework.data.jpa.repository.Query("""
            SELECT new org.apollo.api.dto.SearchResultDTO(cu.id, cu.name, 'Company Unit')
            FROM CompanyUnit cu
            WHERE cu.company.id = :companyId
              AND LOWER(cu.name) LIKE LOWER(CONCAT('%', :search, '%'))
            ORDER BY cu.name ASC
            """)
    List<org.apollo.api.dto.SearchResultDTO> search(
            @org.springframework.data.repository.query.Param("search") String search,
            @org.springframework.data.repository.query.Param("companyId") Long companyId,
            org.springframework.data.domain.Pageable pageable);
}