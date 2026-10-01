package org.apollo.api.repository;

import org.apollo.api.model.MaintenanceRegister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MaintenanceRegisterRepository
        extends JpaRepository<MaintenanceRegister, Long>, JpaSpecificationExecutor<MaintenanceRegister> {

    @Override
    @EntityGraph(attributePaths = {"warning", "maintenanceType", "technician", "createdBy", "parentMaintenance"})
    Page<MaintenanceRegister> findAll(Specification<MaintenanceRegister> spec, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"warning", "maintenanceType", "technician", "createdBy", "parentMaintenance"})
    Optional<MaintenanceRegister> findById(Long id);

    // [warningId, maintenanceId] dos alertas informados que ja tem OS
    @Query("SELECT m.warning.id, m.id FROM MaintenanceRegister m WHERE m.warning.id IN :warningIds")
    List<Object[]> findIdsByWarningIds(@Param("warningIds") Collection<Long> warningIds);
}
