package org.apollo.api.repository;

import org.apollo.api.model.Warning;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WarningRepository extends JpaRepository<Warning, Long> {
    List<Warning> findAllByPanelBatchCompanyUnitCompanyId(Long companyId);
    Optional<Warning> findByIdAndPanelBatchCompanyUnitCompanyId(Long id, Long companyId);
}