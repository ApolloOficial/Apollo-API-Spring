package org.apollo.api.repository;

import org.apollo.api.model.CompanyUnit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CompanyUnitRepository extends JpaRepository<CompanyUnit, UUID> {
    List<CompanyUnit> findAllByCompanyId(Long companyId);
    Optional<CompanyUnit> findByIdAndCompanyId(UUID id, Long companyId);
    List<CompanyUnit> findBySegmentIdAndCompanyId(Long segmentId, Long companyId);
}