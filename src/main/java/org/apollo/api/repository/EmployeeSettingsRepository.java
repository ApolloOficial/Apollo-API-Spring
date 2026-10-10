package org.apollo.api.repository;

import java.util.UUID;
import org.apollo.api.model.EmployeeSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeSettingsRepository extends JpaRepository<EmployeeSettings, UUID> {
}
