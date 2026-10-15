package org.apollo.api.notification;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.apollo.api.security.TenantContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class NotificationController {

    public record PushTokenRequest(
            @NotBlank @Size(max = 300) String token,
            @NotBlank @Size(max = 10) String platform,
            @Size(max = 80) String deviceName) {
    }

    private final NotificationService service;
    private final TenantContext tenantContext;

    @PostMapping("/devices/push-tokens")
    public ResponseEntity<Void> register(@Valid @RequestBody PushTokenRequest request) {
        service.registerToken(tenantContext.getUserId(), request.token(), request.platform(), request.deviceName());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/devices/push-tokens")
    public ResponseEntity<Void> revoke(@RequestParam("token") String token) {
        service.revokeToken(tenantContext.getUserId(), token);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/notifications")
    public List<Map<String, Object>> list(@RequestParam(defaultValue = "false") boolean unread,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "20") int size) {
        return service.list(tenantContext.getUserId(), unread, page, size);
    }

    @GetMapping("/notifications/unread-count")
    public Map<String, Long> unreadCount() {
        return Map.of("unread", service.unreadCount(tenantContext.getUserId()));
    }

    @PatchMapping("/notifications/{id}/read")
    public ResponseEntity<Void> read(@PathVariable UUID id) {
        service.markRead(tenantContext.getUserId(), id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/notifications/read-all")
    public Map<String, Integer> readAll() {
        return Map.of("updated", service.markAllRead(tenantContext.getUserId()));
    }
}
