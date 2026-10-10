package org.apollo.api.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.LanguageOptionDTO;
import org.apollo.api.dto.LanguageSettingsDTO;
import org.apollo.api.dto.MeDTO;
import org.apollo.api.dto.NotificationPreferencesDTO;
import org.apollo.api.dto.NotificationPreferencesUpdateDTO;
import org.apollo.api.dto.ProfileDTO;
import org.apollo.api.dto.SecuritySettingsDTO;
import org.apollo.api.dto.SettingsSummaryDTO;
import org.apollo.api.enums.PhoneChangeStatusEnum;
import org.apollo.api.exception.BusinessRuleException;
import org.apollo.api.model.EmployeeSettings;
import org.apollo.api.repository.EmployeePhotoRepository;
import org.apollo.api.repository.EmployeeSettingsRepository;
import org.apollo.api.repository.PhoneChangeRequestRepository;
import org.apollo.api.util.PhoneMask;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SettingsService {

    public static final String PHOTO_URL = "/api/v1/settings/photo";

    private static final Map<String, String> LANGUAGES = Map.of(
            "pt-BR", "Português (Brasil)",
            "es", "Español",
            "en", "English");

    private static final List<String> LANGUAGE_ORDER = List.of("pt-BR", "es", "en");

    private static final Map<String, String> ROLE_LABELS = Map.of(
            "TECHNICIAN", "Técnico de Manutenção",
            "OPERATOR", "Operador",
            "ANALYST", "Analista",
            "MANAGER", "Gerente de Filial",
            "ADMINISTRATOR", "Administrador",
            "SUPER_ADMIN", "Administrador da plataforma");

    private final ProfileService profileService;
    private final EmployeeSettingsRepository settingsRepository;
    private final EmployeePhotoRepository photoRepository;
    private final PhoneChangeRequestRepository phoneChangeRequestRepository;

    @Transactional(readOnly = true)
    public SettingsSummaryDTO summary() {
        MeDTO me = profileService.me();
        EmployeeSettings settings = load(me.getUserId());
        boolean hasPhoto = photoRepository.existsById(me.getUserId());
        return new SettingsSummaryDTO(me.getUserId(), me.getFullName(), me.getEmail(), me.getRole(),
                roleLabel(me.getRole()), me.getCompanyUnitName(), hasPhoto, hasPhoto ? PHOTO_URL : null,
                settings.getLanguage());
    }

    @Transactional(readOnly = true)
    public ProfileDTO profile() {
        MeDTO me = profileService.me();
        boolean hasPhoto = photoRepository.existsById(me.getUserId());
        return new ProfileDTO(me.getFullName(), me.getEmail(), me.getCompanyName(), me.getCompanyUnitName(),
                me.getRole(), roleLabel(me.getRole()), hasPhoto, hasPhoto ? PHOTO_URL : null);
    }

    @Transactional(readOnly = true)
    public NotificationPreferencesDTO notifications() {
        return toDto(load(profileService.me().getUserId()));
    }

    @Transactional
    public NotificationPreferencesDTO updateNotifications(NotificationPreferencesUpdateDTO dto) {
        EmployeeSettings settings = load(profileService.me().getUserId());
        if (dto.getPredictiveMaintenance() != null) {
            settings.setNotifyPredictive(dto.getPredictiveMaintenance());
        }
        if (dto.getPanelAlert() != null) {
            settings.setNotifyPanelAlert(dto.getPanelAlert());
        }
        if (dto.getEmailSummary() != null) {
            settings.setNotifyEmail(dto.getEmailSummary());
        }
        settings.setUpdatedAt(LocalDateTime.now());
        return toDto(settingsRepository.save(settings));
    }

    @Transactional(readOnly = true)
    public LanguageSettingsDTO language() {
        return languageSettings(load(profileService.me().getUserId()).getLanguage());
    }

    @Transactional
    public LanguageSettingsDTO updateLanguage(String language) {
        if (language == null || !LANGUAGES.containsKey(language)) {
            throw new BusinessRuleException("Unsupported language");
        }
        EmployeeSettings settings = load(profileService.me().getUserId());
        settings.setLanguage(language);
        settings.setUpdatedAt(LocalDateTime.now());
        settingsRepository.save(settings);
        return languageSettings(language);
    }

    @Transactional(readOnly = true)
    public SecuritySettingsDTO security() {
        UUID userId = profileService.me().getUserId();
        EmployeeSettings settings = load(userId);
        boolean pending = phoneChangeRequestRepository.existsByEmployeeIdAndStatus(userId,
                PhoneChangeStatusEnum.PENDING);
        return new SecuritySettingsDTO(PhoneMask.mask(settings.getPhone()), pending,
                settings.getPasswordChangedAt());
    }

    @Transactional(readOnly = true)
    public String currentPhone(UUID employeeId) {
        return load(employeeId).getPhone();
    }

    @Transactional
    public void setPhone(UUID employeeId, String phone) {
        EmployeeSettings settings = load(employeeId);
        settings.setPhone(phone);
        settings.setUpdatedAt(LocalDateTime.now());
        settingsRepository.save(settings);
    }

    @Transactional
    public void markPasswordChanged(UUID employeeId) {
        EmployeeSettings settings = load(employeeId);
        settings.setPasswordChangedAt(LocalDateTime.now());
        settings.setUpdatedAt(LocalDateTime.now());
        settingsRepository.save(settings);
    }

    public static String roleLabel(String role) {
        return role == null ? null : ROLE_LABELS.getOrDefault(role, role);
    }

    private EmployeeSettings load(UUID employeeId) {
        return settingsRepository.findById(employeeId).orElseGet(() -> new EmployeeSettings(employeeId));
    }

    private NotificationPreferencesDTO toDto(EmployeeSettings settings) {
        return new NotificationPreferencesDTO(settings.getNotifyPredictive(), settings.getNotifyPanelAlert(),
                settings.getNotifyEmail());
    }

    private LanguageSettingsDTO languageSettings(String selected) {
        List<LanguageOptionDTO> options = LANGUAGE_ORDER.stream()
                .map(code -> new LanguageOptionDTO(code, LANGUAGES.get(code)))
                .toList();
        return new LanguageSettingsDTO(selected, options);
    }
}
