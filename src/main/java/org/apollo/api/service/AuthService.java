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
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class AuthService {
    private static final String INVALID_CREDENTIALS_MESSAGE = "Credenciais inválidas";
    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_SECONDS = 15 * 60;
    private final ConcurrentHashMap<String, AttemptWindow> attempts = new ConcurrentHashMap<>();

    private final AuthUserRepository authUserRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginResponseDTO login(LoginRequestDTO request) {
        String key = request.getEmail().trim().toLowerCase(Locale.ROOT);
        ensureNotBlocked(key);
        var matchingUsers = authUserRepository.findActiveByEmail(request.getEmail()).stream()
                .filter(authUser -> passwordEncoder.matches(request.getPassword(), authUser.getPasswordHash()))
                .map(AuthenticatedUser::new)
                .toList();
        if (matchingUsers.size() != 1) {
            registerFailure(key);
            throw invalidCredentials();
        }
        attempts.remove(key);
        return new LoginResponseDTO(jwtService.generateToken(matchingUsers.getFirst()), "Bearer");
    }

    public void changePassword(UUID employeeId, Long companyId, ChangePasswordDTO dto) {
        Employee employee = employeeRepository.findByIdAndCompanyUnitCompanyId(employeeId, companyId)
                .orElseThrow(this::invalidCredentials);
        if (!passwordEncoder.matches(dto.getCurrentPassword(), employee.getPasswordHash())) {
            throw new BusinessRuleException("Senha atual incorreta");
        }
        if (dto.getCurrentPassword().equals(dto.getNewPassword())) {
            throw new BusinessRuleException("A nova senha deve ser diferente da senha atual");
        }
        employee.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        employeeRepository.save(employee);
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