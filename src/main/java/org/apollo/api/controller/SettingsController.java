package org.apollo.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.LanguageSettingsDTO;
import org.apollo.api.dto.LanguageUpdateDTO;
import org.apollo.api.dto.NotificationPreferencesDTO;
import org.apollo.api.dto.NotificationPreferencesUpdateDTO;
import org.apollo.api.dto.PhoneChangeRequestCreateDTO;
import org.apollo.api.dto.PhoneChangeRequestDTO;
import org.apollo.api.dto.ProfileDTO;
import org.apollo.api.dto.SecuritySettingsDTO;
import org.apollo.api.dto.SessionDTO;
import org.apollo.api.dto.SettingsSummaryDTO;
import org.apollo.api.model.EmployeePhoto;
import org.apollo.api.service.DeviceSessionService;
import org.apollo.api.service.PhoneChangeService;
import org.apollo.api.service.PhotoService;
import org.apollo.api.service.SettingsService;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/settings")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-key")
public class SettingsController {

    private final SettingsService settingsService;
    private final PhotoService photoService;
    private final PhoneChangeService phoneChangeService;
    private final DeviceSessionService deviceSessionService;

    @GetMapping
    @Operation(summary = "Settings home: name, email, role, branch, photo and language of the logged user")
    public SettingsSummaryDTO summary() {
        return settingsService.summary();
    }

    @GetMapping("/profile")
    @Operation(summary = "My profile (read only): name, email, company, branch and role")
    public ProfileDTO profile() {
        return settingsService.profile();
    }

    @GetMapping("/photo")
    @Operation(summary = "Profile photo of the logged user")
    public ResponseEntity<byte[]> photo() {
        EmployeePhoto photo = photoService.current();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(photo.getContentType()))
                .cacheControl(CacheControl.noCache())
                .body(photo.getData());
    }

    @PutMapping(value = "/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Upload the profile photo (JPEG or PNG, up to 1 MB)")
    public void uploadPhoto(@RequestParam("file") MultipartFile file) {
        photoService.upload(file);
    }

    @DeleteMapping("/photo")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove the profile photo")
    public void removePhoto() {
        photoService.remove();
    }

    @GetMapping("/notifications")
    @Operation(summary = "Notification preferences")
    public NotificationPreferencesDTO notifications() {
        return settingsService.notifications();
    }

    @PatchMapping("/notifications")
    @Operation(summary = "Update notification preferences (only the sent fields change)")
    public NotificationPreferencesDTO updateNotifications(@RequestBody NotificationPreferencesUpdateDTO dto) {
        return settingsService.updateNotifications(dto);
    }

    @GetMapping("/language")
    @Operation(summary = "Selected language and available options")
    public LanguageSettingsDTO language() {
        return settingsService.language();
    }

    @PutMapping("/language")
    @Operation(summary = "Change the language (pt-BR, es or en)")
    public LanguageSettingsDTO updateLanguage(@Valid @RequestBody LanguageUpdateDTO dto) {
        return settingsService.updateLanguage(dto.getLanguage());
    }

    @GetMapping("/security")
    @Operation(summary = "Security: masked SMS phone, pending phone change and last password change")
    public SecuritySettingsDTO security() {
        return settingsService.security();
    }

    @PostMapping("/security/phone-change-requests")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Request a phone change; a manager of the branch approves or rejects it")
    public PhoneChangeRequestDTO requestPhoneChange(@Valid @RequestBody PhoneChangeRequestCreateDTO dto) {
        return phoneChangeService.request(dto);
    }

    @GetMapping("/sessions")
    @Operation(summary = "Devices that logged in with this account (current device is flagged)")
    public List<SessionDTO> sessions() {
        return deviceSessionService.list();
    }
}
