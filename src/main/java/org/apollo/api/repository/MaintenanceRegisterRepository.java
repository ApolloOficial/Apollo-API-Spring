package org.apollo.api.repository;

import org.apollo.api.model.MaintenanceRegister;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MaintenanceRegisterRepository extends JpaRepository<MaintenanceRegister, Long> {
    List<MaintenanceRegister> findAllByBatchCompanyUnitCompanyId(Long companyId);
    Optional<MaintenanceRegister> findByIdAndBatchCompanyUnitCompanyId(Long id, Long companyId);
}