package org.apollo.api.recovery;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apollo.api.exception.BusinessRuleException;
import org.apollo.api.model.Employee;
import org.apollo.api.recovery.RecoveryDtos.OtpSendRequest;
import org.apollo.api.recovery.RecoveryDtos.OtpSentResponse;
import org.apollo.api.recovery.RecoveryDtos.OtpVerifyRequest;
import org.apollo.api.recovery.RecoveryDtos.OtpVerifyResponse;
import org.apollo.api.recovery.RecoveryDtos.PasswordResetRequest;
import org.apollo.api.recovery.RecoveryDtos.RecoveryMethod;
import org.apollo.api.recovery.RecoveryDtos.RecoveryStartRequest;
import org.apollo.api.recovery.RecoveryDtos.RecoveryStartResponse;
import org.apollo.api.repository.AuthUserRepository;
import org.apollo.api.repository.EmployeeRepository;
import org.apollo.api.security.AuthUser;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(noRollbackFor = {ResponseStatusException.class, BusinessRuleException.class})
public class PasswordRecoveryService {

    static final String METHOD_EMAIL = "EMAIL";
    static final int MAX_ATTEMPTS = 5;
    static final int MAX_SENDS = 5;
    static final int MAX_STARTS_PER_HOUR = 5;
    static final Duration CHALLENGE_LIFETIME = Duration.ofMinutes(30);
    static final Duration CODE_LIFETIME = Duration.ofMinutes(10);
    static final Duration RESET_LIFETIME = Duration.ofMinutes(10);
    static final Duration RESEND_INTERVAL = Duration.ofSeconds(60);

    private final AuthUserRepository authUserRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordRecoveryRepository recoveryRepository;
    private final PasswordEncoder passwordEncoder;
    private final RecoveryMailSender mailSender;

    public RecoveryStartResponse start(RecoveryStartRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        List<AuthUser> users = authUserRepository.findActiveByEmail(email);
        UUID employeeId = users.size() == 1 ? users.getFirst().getUserId() : null;

        if (employeeId != null) {
            long recent = recoveryRepository.countByEmployeeIdAndCreatedAtAfter(
                    employeeId, Instant.now().minus(Duration.ofHours(1)));
            if (recent >= MAX_STARTS_PER_HOUR) {
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many recovery requests");
            }
            recoveryRepository.consumeOpenByEmployee(employeeId);
        }

        PasswordRecovery recovery = new PasswordRecovery();
        recovery.setEmployeeId(employeeId);
        recovery.setExpiresAt(Instant.now().plus(CHALLENGE_LIFETIME));
        recovery = recoveryRepository.save(recovery);

        return new RecoveryStartResponse(
                recovery.getId().toString(),
                List.of(new RecoveryMethod(METHOD_EMAIL, EmailMask.mask(email))));
    }

    public OtpSentResponse sendCode(OtpSendRequest request) {
        if (!METHOD_EMAIL.equalsIgnoreCase(request.method())) {
            throw new BusinessRuleException("Method not available");
        }
        PasswordRecovery recovery = openChallenge(request.challengeId());
        Instant now = Instant.now();

        if (recovery.getLastSentAt() != null && recovery.getLastSentAt().plus(RESEND_INTERVAL).isAfter(now)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Wait before requesting a new code");
        }
        if (recovery.getSendCount() >= MAX_SENDS) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many codes requested");
        }

        String code = RecoveryCodes.numericCode();
        Optional<Employee> employee = recovery.getEmployeeId() == null
                ? Optional.empty()
                : employeeRepository.findById(recovery.getEmployeeId());

        recovery.setCodeHash(passwordEncoder.encode(code));
        recovery.setCodeExpiresAt(now.plus(CODE_LIFETIME));
        recovery.setAttempts(0);
        recovery.setSendCount(recovery.getSendCount() + 1);
        recovery.setLastSentAt(now);
        recoveryRepository.save(recovery);

        String destination = employee.map(found -> EmailMask.mask(found.getEmail())).orElse("");
        if (employee.isPresent()) {
            try {
                mailSender.sendCode(employee.get().getEmail(), employee.get().getFullName(), code,
                        (int) CODE_LIFETIME.toMinutes());
            } catch (RuntimeException exception) {
                log.error("Could not send recovery email: {}", exception.getMessage());
                recovery.setCodeHash(null);
                recovery.setCodeExpiresAt(null);
                recovery.setLastSentAt(null);
                recovery.setSendCount(Math.max(0, recovery.getSendCount() - 1));
                recoveryRepository.save(recovery);
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Could not send the code");
            }
        }
        return new OtpSentResponse(destination, (int) RESEND_INTERVAL.toSeconds(), (int) CODE_LIFETIME.toSeconds());
    }

    public OtpVerifyResponse verifyCode(OtpVerifyRequest request) {
        PasswordRecovery recovery = openChallenge(request.challengeId());
        Instant now = Instant.now();

        if (recovery.getCodeHash() == null || recovery.getCodeExpiresAt() == null
                || recovery.getCodeExpiresAt().isBefore(now)) {
            throw new ResponseStatusException(HttpStatus.GONE, "Code expired");
        }
        if (recovery.getAttempts() >= MAX_ATTEMPTS) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many attempts");
        }

        if (recovery.getEmployeeId() == null
                || !passwordEncoder.matches(request.code().trim(), recovery.getCodeHash())) {
            recovery.setAttempts(recovery.getAttempts() + 1);
            if (recovery.getAttempts() >= MAX_ATTEMPTS) {
                recovery.setCodeHash(null);
                recoveryRepository.save(recovery);
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many attempts");
            }
            recoveryRepository.save(recovery);
            throw new BusinessRuleException("Invalid code");
        }

        String token = RecoveryCodes.resetToken();
        recovery.setCodeHash(null);
        recovery.setCodeExpiresAt(null);
        recovery.setResetTokenHash(RecoveryCodes.sha256(token));
        recovery.setResetExpiresAt(now.plus(RESET_LIFETIME));
        recoveryRepository.save(recovery);
        return new OtpVerifyResponse(token);
    }

    public void resetPassword(PasswordResetRequest request) {
        PasswordRecovery recovery = recoveryRepository
                .findByResetTokenHashAndConsumedFalse(RecoveryCodes.sha256(request.resetToken()))
                .filter(found -> found.getResetExpiresAt() != null && found.getResetExpiresAt().isAfter(Instant.now()))
                .orElseThrow(() -> new BusinessRuleException("Reset not allowed"));

        Employee employee = employeeRepository.findById(recovery.getEmployeeId())
                .orElseThrow(() -> new BusinessRuleException("Reset not allowed"));

        employee.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        employeeRepository.save(employee);

        recovery.setConsumed(true);
        recovery.setResetTokenHash(null);
        recoveryRepository.save(recovery);
        recoveryRepository.consumeOpenByEmployee(employee.getId());

        try {
            mailSender.sendPasswordChanged(employee.getEmail(), employee.getFullName());
        } catch (RuntimeException exception) {
            log.warn("Could not send password changed notice: {}", exception.getMessage());
        }
    }

    private PasswordRecovery openChallenge(String challengeId) {
        UUID id;
        try {
            id = UUID.fromString(challengeId);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Challenge not found");
        }
        return recoveryRepository.findById(id)
                .filter(found -> !found.isConsumed() && found.getExpiresAt().isAfter(Instant.now()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Challenge not found"));
    }
}
