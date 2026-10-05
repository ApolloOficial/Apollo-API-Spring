package org.apollo.api.recovery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.apollo.api.exception.BusinessRuleException;
import org.apollo.api.model.Employee;
import org.apollo.api.recovery.RecoveryDtos.OtpSendRequest;
import org.apollo.api.recovery.RecoveryDtos.OtpSentResponse;
import org.apollo.api.recovery.RecoveryDtos.OtpVerifyRequest;
import org.apollo.api.recovery.RecoveryDtos.OtpVerifyResponse;
import org.apollo.api.recovery.RecoveryDtos.PasswordResetRequest;
import org.apollo.api.recovery.RecoveryDtos.RecoveryStartRequest;
import org.apollo.api.recovery.RecoveryDtos.RecoveryStartResponse;
import org.apollo.api.repository.AuthUserRepository;
import org.apollo.api.repository.EmployeeRepository;
import org.apollo.api.security.AuthUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PasswordRecoveryServiceTest {

    @Mock
    private AuthUserRepository authUserRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private PasswordRecoveryRepository recoveryRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RecoveryMailSender mailSender;

    private PasswordRecoveryService service;
    private UUID employeeId;
    private Employee employee;

    @BeforeEach
    void setUp() {
        service = new PasswordRecoveryService(authUserRepository, employeeRepository, recoveryRepository,
                passwordEncoder, mailSender);
        employeeId = UUID.randomUUID();
        employee = new Employee();
        employee.setId(employeeId);
        employee.setFullName("Enzo Mota");
        employee.setEmail("enzo.mota@empresa.com");
        employee.setPasswordHash("old-hash");
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(passwordEncoder.encode(anyString())).thenAnswer(invocation -> "enc:" + invocation.getArgument(0));
        when(passwordEncoder.matches(anyString(), anyString()))
                .thenAnswer(invocation -> ("enc:" + invocation.getArgument(0)).equals(invocation.getArgument(1)));
        when(recoveryRepository.save(any(PasswordRecovery.class))).thenAnswer(invocation -> {
            PasswordRecovery saved = invocation.getArgument(0);
            if (saved.getId() == null) {
                setId(saved, UUID.randomUUID());
            }
            return saved;
        });
    }

    @Test
    void shouldStartRecoveryForKnownEmailWithMaskedDestination() {
        AuthUser user = Mockito.mock(AuthUser.class);
        when(user.getUserId()).thenReturn(employeeId);
        when(authUserRepository.findActiveByEmail("enzo.mota@empresa.com")).thenReturn(List.of(user));

        RecoveryStartResponse response = service.start(new RecoveryStartRequest(" Enzo.Mota@empresa.com "));

        assertNotNull(response.challengeId());
        assertEquals("EMAIL", response.methods().getFirst().method());
        assertEquals("e***@empresa.com", response.methods().getFirst().destination());
        verify(recoveryRepository).consumeOpenByEmployee(employeeId);
    }

    @Test
    void shouldAnswerTheSameWayForUnknownEmail() {
        when(authUserRepository.findActiveByEmail("ghost@x.com")).thenReturn(List.of());

        RecoveryStartResponse response = service.start(new RecoveryStartRequest("ghost@x.com"));

        assertNotNull(response.challengeId());
        assertEquals("g***@x.com", response.methods().getFirst().destination());
        verify(recoveryRepository, never()).consumeOpenByEmployee(any());
    }

    @Test
    void shouldRejectTooManyRecoveryStarts() {
        AuthUser user = Mockito.mock(AuthUser.class);
        when(user.getUserId()).thenReturn(employeeId);
        when(authUserRepository.findActiveByEmail("enzo.mota@empresa.com")).thenReturn(List.of(user));
        when(recoveryRepository.countByEmployeeIdAndCreatedAtAfter(eq(employeeId), any())).thenReturn(5L);

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.start(new RecoveryStartRequest("enzo.mota@empresa.com")));

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, error.getStatusCode());
    }

    @Test
    void shouldSendCodeByEmailAndStoreOnlyItsHash() {
        PasswordRecovery recovery = openRecovery(employeeId);

        OtpSentResponse response = service.sendCode(new OtpSendRequest(recovery.getId().toString(), "email"));

        ArgumentCaptor<String> code = ArgumentCaptor.forClass(String.class);
        verify(mailSender).sendCode(eq("enzo.mota@empresa.com"), eq("Enzo Mota"), code.capture(), eq(10));
        assertTrue(code.getValue().matches("\\d{6}"));
        assertEquals("enc:" + code.getValue(), recovery.getCodeHash());
        assertEquals("e***@empresa.com", response.destination());
        assertEquals(60, response.resendInSeconds());
        assertEquals(600, response.expiresInSeconds());
        assertEquals(1, recovery.getSendCount());
    }

    @Test
    void shouldNotSendEmailForUnknownAccountButAnswerNormally() {
        PasswordRecovery recovery = openRecovery(null);

        OtpSentResponse response = service.sendCode(new OtpSendRequest(recovery.getId().toString(), "EMAIL"));

        verify(mailSender, never()).sendCode(anyString(), anyString(), anyString(), anyInt());
        assertEquals(60, response.resendInSeconds());
        assertNotNull(recovery.getCodeHash());
    }

    @Test
    void shouldRejectSmsMethod() {
        PasswordRecovery recovery = openRecovery(employeeId);

        assertThrows(BusinessRuleException.class,
                () -> service.sendCode(new OtpSendRequest(recovery.getId().toString(), "SMS")));
    }

    @Test
    void shouldRejectResendBeforeInterval() {
        PasswordRecovery recovery = openRecovery(employeeId);
        recovery.setLastSentAt(Instant.now().minusSeconds(10));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.sendCode(new OtpSendRequest(recovery.getId().toString(), "EMAIL")));

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, error.getStatusCode());
        verify(mailSender, never()).sendCode(anyString(), anyString(), anyString(), anyInt());
    }

    @Test
    void shouldUndoTheSendWhenTheEmailFails() {
        PasswordRecovery recovery = openRecovery(employeeId);
        Mockito.doThrow(new IllegalStateException("smtp down"))
                .when(mailSender).sendCode(anyString(), anyString(), anyString(), anyInt());

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.sendCode(new OtpSendRequest(recovery.getId().toString(), "EMAIL")));

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, error.getStatusCode());
        assertNull(recovery.getCodeHash());
        assertNull(recovery.getLastSentAt());
        assertEquals(0, recovery.getSendCount());
    }

    @Test
    void shouldReturnNotFoundForUnknownOrMalformedChallenge() {
        ResponseStatusException malformed = assertThrows(ResponseStatusException.class,
                () -> service.sendCode(new OtpSendRequest("not-a-uuid", "EMAIL")));
        assertEquals(HttpStatus.NOT_FOUND, malformed.getStatusCode());

        UUID missing = UUID.randomUUID();
        when(recoveryRepository.findById(missing)).thenReturn(Optional.empty());
        ResponseStatusException unknown = assertThrows(ResponseStatusException.class,
                () -> service.verifyCode(new OtpVerifyRequest(missing.toString(), "123456")));
        assertEquals(HttpStatus.NOT_FOUND, unknown.getStatusCode());
    }

    @Test
    void shouldIssueResetTokenWhenCodeMatches() {
        PasswordRecovery recovery = openRecovery(employeeId);
        recovery.setCodeHash("enc:123456");
        recovery.setCodeExpiresAt(Instant.now().plusSeconds(300));

        OtpVerifyResponse response = service.verifyCode(new OtpVerifyRequest(recovery.getId().toString(), " 123456 "));

        assertNotNull(response.resetToken());
        assertEquals(RecoveryCodes.sha256(response.resetToken()), recovery.getResetTokenHash());
        assertNull(recovery.getCodeHash());
        assertNotNull(recovery.getResetExpiresAt());
    }

    @Test
    void shouldCountWrongCodeAndBlockAfterFiveAttempts() {
        PasswordRecovery recovery = openRecovery(employeeId);
        recovery.setCodeHash("enc:123456");
        recovery.setCodeExpiresAt(Instant.now().plusSeconds(300));
        OtpVerifyRequest wrong = new OtpVerifyRequest(recovery.getId().toString(), "000000");

        for (int i = 0; i < 4; i++) {
            assertThrows(BusinessRuleException.class, () -> service.verifyCode(wrong));
        }
        assertEquals(4, recovery.getAttempts());

        ResponseStatusException blocked = assertThrows(ResponseStatusException.class, () -> service.verifyCode(wrong));
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, blocked.getStatusCode());
        assertNull(recovery.getCodeHash());
    }

    @Test
    void shouldReportExpiredCode() {
        PasswordRecovery recovery = openRecovery(employeeId);
        recovery.setCodeHash("enc:123456");
        recovery.setCodeExpiresAt(Instant.now().minusSeconds(1));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.verifyCode(new OtpVerifyRequest(recovery.getId().toString(), "123456")));

        assertEquals(HttpStatus.GONE, error.getStatusCode());
    }

    @Test
    void shouldTreatUnknownAccountLikeAWrongCode() {
        PasswordRecovery recovery = openRecovery(null);
        recovery.setCodeHash("enc:123456");
        recovery.setCodeExpiresAt(Instant.now().plusSeconds(300));

        assertThrows(BusinessRuleException.class,
                () -> service.verifyCode(new OtpVerifyRequest(recovery.getId().toString(), "123456")));
    }

    @Test
    void shouldResetPasswordOnceWithValidToken() {
        PasswordRecovery recovery = openRecovery(employeeId);
        String token = "reset-token";
        recovery.setResetTokenHash(RecoveryCodes.sha256(token));
        recovery.setResetExpiresAt(Instant.now().plusSeconds(300));
        when(recoveryRepository.findByResetTokenHashAndConsumedFalse(RecoveryCodes.sha256(token)))
                .thenReturn(Optional.of(recovery));

        service.resetPassword(new PasswordResetRequest(token, "NovaSenha123"));

        assertEquals("enc:NovaSenha123", employee.getPasswordHash());
        assertTrue(recovery.isConsumed());
        assertNull(recovery.getResetTokenHash());
        verify(employeeRepository).save(employee);
        verify(mailSender).sendPasswordChanged("enzo.mota@empresa.com", "Enzo Mota");
    }

    @Test
    void shouldRejectUnknownOrExpiredResetToken() {
        when(recoveryRepository.findByResetTokenHashAndConsumedFalse(anyString())).thenReturn(Optional.empty());
        assertThrows(BusinessRuleException.class,
                () -> service.resetPassword(new PasswordResetRequest("nope", "NovaSenha123")));

        PasswordRecovery expired = openRecovery(employeeId);
        expired.setResetTokenHash(RecoveryCodes.sha256("old"));
        expired.setResetExpiresAt(Instant.now().minusSeconds(1));
        when(recoveryRepository.findByResetTokenHashAndConsumedFalse(RecoveryCodes.sha256("old")))
                .thenReturn(Optional.of(expired));
        assertThrows(BusinessRuleException.class,
                () -> service.resetPassword(new PasswordResetRequest("old", "NovaSenha123")));
        assertEquals("old-hash", employee.getPasswordHash());
        assertFalse(expired.isConsumed());
    }

    @Test
    void shouldStillResetWhenNoticeEmailFails() {
        PasswordRecovery recovery = openRecovery(employeeId);
        recovery.setResetTokenHash(RecoveryCodes.sha256("t"));
        recovery.setResetExpiresAt(Instant.now().plusSeconds(300));
        when(recoveryRepository.findByResetTokenHashAndConsumedFalse(RecoveryCodes.sha256("t")))
                .thenReturn(Optional.of(recovery));
        Mockito.doThrow(new IllegalStateException("smtp down"))
                .when(mailSender).sendPasswordChanged(anyString(), anyString());

        service.resetPassword(new PasswordResetRequest("t", "NovaSenha123"));

        assertEquals("enc:NovaSenha123", employee.getPasswordHash());
    }

    private PasswordRecovery openRecovery(UUID owner) {
        PasswordRecovery recovery = new PasswordRecovery();
        setId(recovery, UUID.randomUUID());
        recovery.setEmployeeId(owner);
        recovery.setExpiresAt(Instant.now().plusSeconds(1800));
        when(recoveryRepository.findById(recovery.getId())).thenReturn(Optional.of(recovery));
        return recovery;
    }

    private static void setId(PasswordRecovery recovery, UUID id) {
        try {
            Field field = PasswordRecovery.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(recovery, id);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
