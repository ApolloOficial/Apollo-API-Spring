package org.apollo.api.repository;

import org.apollo.api.model.CompanyUnit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CompanyUnitRepository extends JpaRepository<CompanyUnit, Long> {
    List<CompanyUnit> findAllByCompanyId(Long companyId);
    Optional<CompanyUnit> findByIdAndCompanyId(Long id, Long companyId);
    List<CompanyUnit> findBySegmentIdAndCompanyId(Long segmentId, Long companyId);
}
