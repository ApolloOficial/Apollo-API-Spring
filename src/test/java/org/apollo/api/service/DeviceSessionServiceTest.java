package org.apollo.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.apollo.api.dto.SessionDTO;
import org.apollo.api.model.DeviceSession;
import org.apollo.api.repository.DeviceSessionRepository;
import org.apollo.api.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@ExtendWith(MockitoExtension.class)
class DeviceSessionServiceTest {

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Mock
    private TenantContext tenantContext;

    @Mock
    private DeviceSessionRepository deviceSessionRepository;

    @InjectMocks
    private DeviceSessionService deviceSessionService;

    @AfterEach
    void clearRequest() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void shouldNormalizeDeviceNames() {
        assertEquals(DeviceSessionService.UNKNOWN_DEVICE, DeviceSessionService.normalize(null));
        assertEquals(DeviceSessionService.UNKNOWN_DEVICE, DeviceSessionService.normalize("   "));
        assertEquals("Galaxy S23", DeviceSessionService.normalize("  Galaxy S23 "));
        assertEquals(DeviceSessionService.MAX_NAME_LENGTH,
                DeviceSessionService.normalize("x".repeat(200)).length());
    }

    @Test
    void shouldCreateSessionWithTheDeviceFromTheHeader() {
        useDeviceHeader("Galaxy S23");
        when(deviceSessionRepository.findByEmployeeIdAndDeviceName(USER_ID, "Galaxy S23"))
                .thenReturn(Optional.empty());

        deviceSessionService.registerLogin(USER_ID);

        ArgumentCaptor<DeviceSession> captor = ArgumentCaptor.forClass(DeviceSession.class);
        verify(deviceSessionRepository).save(captor.capture());
        assertEquals("Galaxy S23", captor.getValue().getDeviceName());
        assertEquals(USER_ID, captor.getValue().getEmployeeId());
    }

    @Test
    void shouldReuseSessionOfTheSameDevice() {
        useDeviceHeader("Galaxy S23");
        DeviceSession existing = new DeviceSession(USER_ID, "Galaxy S23");
        when(deviceSessionRepository.findByEmployeeIdAndDeviceName(USER_ID, "Galaxy S23"))
                .thenReturn(Optional.of(existing));

        deviceSessionService.registerLogin(USER_ID);

        verify(deviceSessionRepository).save(existing);
    }

    @Test
    void shouldFlagTheCurrentDeviceInTheList() {
        useDeviceHeader("Galaxy S23");
        when(tenantContext.getUserId()).thenReturn(USER_ID);
        DeviceSession current = new DeviceSession(USER_ID, "Galaxy S23");
        DeviceSession other = new DeviceSession(USER_ID, "Pixel 8");
        when(deviceSessionRepository.findByEmployeeIdOrderByLastLoginAtDesc(USER_ID))
                .thenReturn(List.of(current, other));

        List<SessionDTO> sessions = deviceSessionService.list();

        assertTrue(sessions.get(0).current());
        assertFalse(sessions.get(1).current());
    }

    private void useDeviceHeader(String name) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(DeviceSessionService.DEVICE_HEADER, name);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }
}
