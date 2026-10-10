package org.apollo.api.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.SessionDTO;
import org.apollo.api.model.DeviceSession;
import org.apollo.api.repository.DeviceSessionRepository;
import org.apollo.api.security.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@RequiredArgsConstructor
public class DeviceSessionService {

    public static final String DEVICE_HEADER = "X-Device-Name";
    public static final String UNKNOWN_DEVICE = "Unknown device";
    static final int MAX_NAME_LENGTH = 80;

    private final TenantContext tenantContext;
    private final DeviceSessionRepository deviceSessionRepository;

    @Transactional
    public void registerLogin(UUID employeeId) {
        String deviceName = currentDeviceName();
        LocalDateTime now = LocalDateTime.now();
        DeviceSession session = deviceSessionRepository.findByEmployeeIdAndDeviceName(employeeId, deviceName)
                .orElseGet(() -> new DeviceSession(employeeId, deviceName));
        session.setLastLoginAt(now);
        session.setLastSeenAt(now);
        deviceSessionRepository.save(session);
    }

    @Transactional
    public List<SessionDTO> list() {
        UUID userId = tenantContext.getUserId();
        String currentName = currentDeviceName();
        LocalDateTime now = LocalDateTime.now();
        return deviceSessionRepository.findByEmployeeIdOrderByLastLoginAtDesc(userId).stream()
                .map(session -> {
                    boolean current = session.getDeviceName().equalsIgnoreCase(currentName);
                    if (current) {
                        session.setLastSeenAt(now);
                        deviceSessionRepository.save(session);
                    }
                    return new SessionDTO(session.getId(), session.getDeviceName(), current,
                            session.getFirstLoginAt(), session.getLastLoginAt(), session.getLastSeenAt());
                })
                .toList();
    }

    static String normalize(String raw) {
        if (raw == null || raw.isBlank()) {
            return UNKNOWN_DEVICE;
        }
        String cleaned = raw.trim().replaceAll("[\\p{Cntrl}]", "");
        if (cleaned.isEmpty()) {
            return UNKNOWN_DEVICE;
        }
        return cleaned.length() > MAX_NAME_LENGTH ? cleaned.substring(0, MAX_NAME_LENGTH) : cleaned;
    }

    private String currentDeviceName() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return normalize(attributes.getRequest().getHeader(DEVICE_HEADER));
        }
        return UNKNOWN_DEVICE;
    }
}
