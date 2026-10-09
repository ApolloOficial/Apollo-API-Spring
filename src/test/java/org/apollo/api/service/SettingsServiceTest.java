package org.apollo.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import org.apollo.api.dto.MeDTO;
import org.apollo.api.dto.NotificationPreferencesDTO;
import org.apollo.api.dto.NotificationPreferencesUpdateDTO;
import org.apollo.api.dto.SecuritySettingsDTO;
import org.apollo.api.dto.SettingsSummaryDTO;
import org.apollo.api.enums.PhoneChangeStatusEnum;
import org.apollo.api.exception.BusinessRuleException;
import org.apollo.api.model.EmployeeSettings;
import org.apollo.api.repository.EmployeePhotoRepository;
import org.apollo.api.repository.EmployeeSettingsRepository;
import org.apollo.api.repository.PhoneChangeRequestRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SettingsServiceTest {

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Mock
    private ProfileService profileService;

    @Mock
    private EmployeeSettingsRepository settingsRepository;

    @Mock
    private EmployeePhotoRepository photoRepository;

    @Mock
    private PhoneChangeRequestRepository phoneChangeRequestRepository;

    @InjectMocks
    private SettingsService settingsService;

    @Test
    void shouldBuildSummaryWithRoleLabelAndDefaultLanguage() {
        when(profileService.me()).thenReturn(me());
        when(settingsRepository.findById(USER_ID)).thenReturn(Optional.empty());
        when(photoRepository.existsById(USER_ID)).thenReturn(true);

        SettingsSummaryDTO summary = settingsService.summary();

        assertEquals("Técnico de Manutenção", summary.roleLabel());
        assertEquals("pt-BR", summary.language());
        assertTrue(summary.hasPhoto());
        assertEquals(SettingsService.PHOTO_URL, summary.photoUrl());
    }

    @Test
    void shouldNotExposePhotoUrlWhenThereIsNoPhoto() {
        when(profileService.me()).thenReturn(me());
        when(settingsRepository.findById(USER_ID)).thenReturn(Optional.empty());
        when(photoRepository.existsById(USER_ID)).thenReturn(false);

        SettingsSummaryDTO summary = settingsService.summary();

        assertFalse(summary.hasPhoto());
        assertNull(summary.photoUrl());
    }

    @Test
    void shouldChangeOnlyTheSentNotificationFields() {
        when(profileService.me()).thenReturn(me());
        when(settingsRepository.findById(USER_ID)).thenReturn(Optional.empty());
        when(settingsRepository.save(any(EmployeeSettings.class))).thenAnswer(call -> call.getArgument(0));

        NotificationPreferencesDTO result = settingsService
                .updateNotifications(new NotificationPreferencesUpdateDTO(false, null, null));

        assertFalse(result.predictiveMaintenance());
        assertTrue(result.panelAlert());
        assertFalse(result.emailSummary());
    }

    @Test
    void shouldRejectUnsupportedLanguage() {
        assertThrows(BusinessRuleException.class, () -> settingsService.updateLanguage("fr"));
    }

    @Test
    void shouldSaveSupportedLanguage() {
        when(profileService.me()).thenReturn(me());
        when(settingsRepository.findById(USER_ID)).thenReturn(Optional.empty());

        var result = settingsService.updateLanguage("en");

        ArgumentCaptor<EmployeeSettings> captor = ArgumentCaptor.forClass(EmployeeSettings.class);
        verify(settingsRepository).save(captor.capture());
        assertEquals("en", captor.getValue().getLanguage());
        assertEquals("en", result.selected());
        assertEquals(3, result.options().size());
    }

    @Test
    void shouldMaskPhoneAndReportPendingRequest() {
        EmployeeSettings settings = new EmployeeSettings(USER_ID);
        settings.setPhone("85987654321");
        when(profileService.me()).thenReturn(me());
        when(settingsRepository.findById(USER_ID)).thenReturn(Optional.of(settings));
        when(phoneChangeRequestRepository.existsByEmployeeIdAndStatus(USER_ID, PhoneChangeStatusEnum.PENDING))
                .thenReturn(true);

        SecuritySettingsDTO security = settingsService.security();

        assertEquals("(85) 9****-4321", security.maskedPhone());
        assertTrue(security.phoneChangePending());
        assertNull(security.passwordChangedAt());
    }

    @Test
    void shouldRecordPasswordChangeTime() {
        when(settingsRepository.findById(USER_ID)).thenReturn(Optional.empty());

        settingsService.markPasswordChanged(USER_ID);

        ArgumentCaptor<EmployeeSettings> captor = ArgumentCaptor.forClass(EmployeeSettings.class);
        verify(settingsRepository).save(captor.capture());
        assertNotNull(captor.getValue().getPasswordChangedAt());
    }

    @Test
    void shouldFallBackToRoleNameWhenThereIsNoLabel() {
        assertEquals("CUSTOM", SettingsService.roleLabel("CUSTOM"));
        assertNull(SettingsService.roleLabel(null));
    }

    private MeDTO me() {
        return new MeDTO(USER_ID, "Carlos Alves da Silva", "carlos@apollo.com", "TECHNICIAN", 10L,
                "Apollo Solar", UUID.fromString("00000000-0000-0000-0000-0000000000aa"), "Fábrica Itajaí");
    }
}
