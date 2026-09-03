package org.apollo.api.repository;

import org.apollo.api.model.Batch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BatchRepository extends JpaRepository<Batch, Long> {
    List<Batch> findAllByCompanyId(Long companyId);
    Optional<Batch> findByIdAndCompanyId(Long id, Long companyId);
    boolean existsByCompanyIdAndBillNumber(Long companyId, String billNumber);
    boolean existsByCompanyIdAndBillNumberAndIdNot(Long companyId, String billNumber, Long id);
}
