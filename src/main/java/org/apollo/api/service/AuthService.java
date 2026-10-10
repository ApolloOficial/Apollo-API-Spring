package org.apollo.api.service;

import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.ChangePasswordDTO;
import org.apollo.api.dto.LoginRequestDTO;
import org.apollo.api.dto.LoginResponseDTO;
import org.apollo.api.exception.BusinessRuleException;
import org.apollo.api.model.Employee;
import org.apollo.api.repository.AuthUserRepository;
import org.apollo.api.repository.EmployeeRepository;
import org.apollo.api.security.AuthenticatedUser;
import org.apollo.api.util.DbProcedures;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class AuthService {
    private static final String INVALID_CREDENTIALS_MESSAGE = "Invalid credentials";
    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_SECONDS = 15 * 60;
    private final ConcurrentHashMap<String, AttemptWindow> attempts = new ConcurrentHashMap<>();

    private final AuthUserRepository authUserRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final DbProcedures dbProcedures;
    private final SettingsService settingsService;
    private final DeviceSessionService deviceSessionService;

    public LoginResponseDTO login(LoginRequestDTO request) {
        String key = request.getEmail().trim().toLowerCase(Locale.ROOT);
        ensureNotBlocked(key);
        var matchingUsers = authUserRepository.findActiveByEmail(request.getEmail()).stream()
                .filter(authUser -> passwordEncoder.matches(request.getPassword(), authUser.getPasswordHash()))
                .map(AuthenticatedUser::new)
                .toList();
        if (matchingUsers.size() != 1) {
            registerFailure(key);
            registerAccess(request.getEmail(), "FALHA");
            throw invalidCredentials();
        }
        attempts.remove(key);
        registerAccess(request.getEmail(), "SUCESSO");
        AuthenticatedUser user = matchingUsers.getFirst();
        registerDevice(user.getUserId());
        return new LoginResponseDTO(jwtService.generateToken(user), "Bearer", user.isFirstAccess());
    }

    public void changePassword(UUID employeeId, Long companyId, ChangePasswordDTO dto) {
        Employee employee = employeeRepository.findByIdAndCompanyUnitCompanyId(employeeId, companyId)
                .orElseThrow(this::invalidCredentials);
        if (!passwordEncoder.matches(dto.getCurrentPassword(), employee.getPasswordHash())) {
            throw new BusinessRuleException("Current password is incorrect");
        }
        if (dto.getCurrentPassword().equals(dto.getNewPassword())) {
            throw new BusinessRuleException("The new password must be different from the current password");
        }
        employee.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        employee.setIsFirstAccess(false);
        employeeRepository.save(employee);
        markPasswordChanged(employeeId);
    }

    private void registerDevice(UUID employeeId) {
        try {
            deviceSessionService.registerLogin(employeeId);
        } catch (RuntimeException ignored) {
        }
    }

    private void markPasswordChanged(UUID employeeId) {
        try {
            settingsService.markPasswordChanged(employeeId);
        } catch (RuntimeException ignored) {
        }
    }

    // Auditoria de acesso (tabela access_log). Nunca pode derrubar o login.
    private void registerAccess(String email, String status) {
        try {
            dbProcedures.registerAccess(email, UUID.randomUUID(), clientIp(), status);
        } catch (RuntimeException ignored) {
            // falha de auditoria nao deve impedir (nem revelar nada sobre) o login
        }
    }

    private String clientIp() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            String forwarded = attributes.getRequest().getHeader("X-Forwarded-For");
            if (forwarded != null && !forwarded.isBlank()) {
                return forwarded.split(",")[0].trim();
            }
            return attributes.getRequest().getRemoteAddr();
        }
        return null;
    }

    private void ensureNotBlocked(String key) {
        AttemptWindow window = attempts.get(key);
        if (window != null && !window.isExpired() && window.attempts >= MAX_ATTEMPTS) throw invalidCredentials();
    }

    private void registerFailure(String key) {
        attempts.compute(key, (ignored, current) -> current == null || current.isExpired()
                ? new AttemptWindow(1, Instant.now())
                : new AttemptWindow(current.attempts + 1, current.startedAt));
    }

    private ResponseStatusException invalidCredentials() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, INVALID_CREDENTIALS_MESSAGE);
    }

    private record AttemptWindow(int attempts, Instant startedAt) {
        boolean isExpired() { return startedAt.plusSeconds(WINDOW_SECONDS).isBefore(Instant.now()); }
    }
}