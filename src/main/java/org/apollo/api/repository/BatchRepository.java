package org.apollo.api.repository;

import org.apollo.api.model.Batch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BatchRepository extends JpaRepository<Batch, UUID> {
    List<Batch> findAllByCompanyUnitCompanyId(Long companyId);
    Optional<Batch> findByIdAndCompanyUnitCompanyId(UUID id, Long companyId);
    boolean existsByCompanyUnitIdAndBillNumber(UUID companyUnitId, String billNumber);
    boolean existsByCompanyUnitIdAndBillNumberAndIdNot(UUID companyUnitId, String billNumber, UUID id);
}
