package org.apollo.api.service;

import org.apollo.api.dto.ChangePasswordDTO;
import org.apollo.api.dto.LoginRequestDTO;
import org.apollo.api.dto.LoginResponseDTO;
import org.apollo.api.model.Employee;
import org.apollo.api.model.Roles;
import org.apollo.api.repository.AuthUserRepository;
import org.apollo.api.repository.EmployeeRepository;
import org.apollo.api.security.AuthUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthUserRepository authUserRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private org.apollo.api.util.DbProcedures dbProcedures;

    @Mock
    private SettingsService settingsService;

    @Mock
    private DeviceSessionService deviceSessionService;

    @InjectMocks
    private AuthService authService;

    @Test
    void shouldReturnBearerTokenWhenCredentialsAreValid() {
        LoginRequestDTO request = new LoginRequestDTO("admin@apollo.com", "password");
        AuthUser authUser = authenticatedUser();
        when(authUserRepository.findActiveByEmail("admin@apollo.com"))
                .thenReturn(List.of(authUser));
        when(passwordEncoder.matches("password", "encoded-password")).thenReturn(true);
        when(jwtService.generateToken(any())).thenReturn("generated-token");

        LoginResponseDTO response = authService.login(request);

        assertEquals("generated-token", response.getToken());
        assertEquals("Bearer", response.getTokenType());
    }

    @Test
    void shouldRejectUnknownEmail() {
        LoginRequestDTO request = new LoginRequestDTO("ghost@apollo.com", "password");
        when(authUserRepository.findActiveByEmail("ghost@apollo.com"))
                .thenReturn(List.of());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> authService.login(request));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertEquals("Invalid credentials", exception.getReason());
    }

    @Test
    void shouldRecordPasswordChangeTime() {
        UUID employeeId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        Employee employee = new Employee();
        employee.setPasswordHash("old-hash");
        when(employeeRepository.findByIdAndCompanyUnitCompanyId(employeeId, 10L)).thenReturn(Optional.of(employee));
        when(passwordEncoder.matches("current-pass", "old-hash")).thenReturn(true);
        when(passwordEncoder.encode("New#Pass123")).thenReturn("new-hash");

        authService.changePassword(employeeId, 10L, new ChangePasswordDTO("current-pass", "New#Pass123"));

        assertEquals("new-hash", employee.getPasswordHash());
        verify(settingsService).markPasswordChanged(employeeId);
    }

    @Test
    void shouldRegisterDeviceAfterSuccessfulLogin() {
        LoginRequestDTO request = new LoginRequestDTO("admin@apollo.com", "password");
        AuthUser user = authenticatedUser();
        when(authUserRepository.findActiveByEmail("admin@apollo.com")).thenReturn(List.of(user));
        when(passwordEncoder.matches("password", "encoded-password")).thenReturn(true);
        when(jwtService.generateToken(any())).thenReturn("generated-token");

        authService.login(request);

        verify(deviceSessionService).registerLogin(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    }

    private AuthUser authenticatedUser() {
        AuthUser user = mock(AuthUser.class);
        when(user.getUserId()).thenReturn(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        when(user.getCompanyId()).thenReturn(10L);
        when(user.getEmail()).thenReturn("admin@apollo.com");
        when(user.getPasswordHash()).thenReturn("encoded-password");
        when(user.isActive()).thenReturn(true);
        when(user.getRole()).thenReturn(new Roles(1L, "MANAGER", null));
        return user;
    }
}