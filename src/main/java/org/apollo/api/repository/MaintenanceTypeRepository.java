package org.apollo.api.repository;

import org.apollo.api.model.MaintenanceType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaintenanceTypeRepository extends JpaRepository<MaintenanceType, Long> {
}