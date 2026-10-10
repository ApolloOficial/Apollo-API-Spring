package org.apollo.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "device_session")
@Getter
@Setter
@NoArgsConstructor
public class DeviceSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "device_name", nullable = false, length = 80)
    private String deviceName;

    @Column(name = "first_login_at", nullable = false)
    private LocalDateTime firstLoginAt = LocalDateTime.now();

    @Column(name = "last_login_at", nullable = false)
    private LocalDateTime lastLoginAt = LocalDateTime.now();

    @Column(name = "last_seen_at", nullable = false)
    private LocalDateTime lastSeenAt = LocalDateTime.now();

    public DeviceSession(UUID employeeId, String deviceName) {
        this.employeeId = employeeId;
        this.deviceName = deviceName;
    }
}
