package org.apollo.api.repository;

import org.apollo.api.model.ClimaticIndex;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClimaticIndexRepository extends JpaRepository<ClimaticIndex, Long> {
    List<ClimaticIndex> findAllByCompanyUnitIdAndCompanyUnitCompanyId(java.util.UUID companyUnitId, Long companyId);
}