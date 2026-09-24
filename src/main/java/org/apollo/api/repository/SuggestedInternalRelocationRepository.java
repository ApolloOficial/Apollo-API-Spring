package org.apollo.api.repository;

import org.apollo.api.model.SuggestedInternalRelocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SuggestedInternalRelocationRepository extends JpaRepository<SuggestedInternalRelocation, Long> {
    List<SuggestedInternalRelocation> findAllByBatchCompanyUnitCompanyId(Long companyId);
    Optional<SuggestedInternalRelocation> findByIdAndBatchCompanyUnitCompanyId(Long id, Long companyId);
}