package org.apollo.api.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.apollo.api.model.DeviceSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceSessionRepository extends JpaRepository<DeviceSession, UUID> {

    Optional<DeviceSession> findByEmployeeIdAndDeviceName(UUID employeeId, String deviceName);

    List<DeviceSession> findByEmployeeIdOrderByLastLoginAtDesc(UUID employeeId);
}
