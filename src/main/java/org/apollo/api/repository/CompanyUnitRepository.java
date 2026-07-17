package org.apollo.api.repository;

import org.apollo.api.model.CompanyUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompanyUnitRepository extends JpaRepository<CompanyUnit, Long> {
    List<CompanyUnit> findBySegmentId(Long segmentId);
}