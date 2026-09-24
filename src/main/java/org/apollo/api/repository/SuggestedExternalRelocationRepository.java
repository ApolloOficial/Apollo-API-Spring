package org.apollo.api.repository;

import org.apollo.api.model.SuggestedExternalRelocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SuggestedExternalRelocationRepository extends JpaRepository<SuggestedExternalRelocation, Long> {
    List<SuggestedExternalRelocation> findAllByPanelBatchCompanyUnitCompanyId(Long companyId);
    Optional<SuggestedExternalRelocation> findByIdAndPanelBatchCompanyUnitCompanyId(Long id, Long companyId);
}