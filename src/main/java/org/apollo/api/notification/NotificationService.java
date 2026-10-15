package org.apollo.api.notification;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.apollo.api.exception.BusinessRuleException;
import org.apollo.api.exception.ResourceNotFoundException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final List<String> PLATFORMS = List.of("WEB", "ANDROID");

    private final JdbcTemplate jdbc;

    /** Registrar token = so guardar no banco. O envio do push e feito pelo PushDispatcher. */
    @Transactional
    public void registerToken(UUID employeeId, String token, String platform, String deviceName) {
        String normalized = platform == null ? "" : platform.trim().toUpperCase();
        if (!PLATFORMS.contains(normalized)) {
            throw new BusinessRuleException("Platform must be WEB or ANDROID");
        }
        jdbc.update("""
                INSERT INTO device_push_token (employee_id, platform, device_name, fcm_token)
                VALUES (?, ?, ?, ?)
                ON CONFLICT (fcm_token) DO UPDATE
                   SET employee_id = EXCLUDED.employee_id, platform = EXCLUDED.platform,
                       device_name = EXCLUDED.device_name, is_active = true, last_seen_at = now()
                """, employeeId, normalized, deviceName, token);
    }

    @Transactional
    public void revokeToken(UUID employeeId, String token) {
        jdbc.update("UPDATE device_push_token SET is_active = false WHERE employee_id = ? AND fcm_token = ?",
                employeeId, token);
    }

    public List<Map<String, Object>> list(UUID employeeId, boolean unreadOnly, int page, int size) {
        int limit = Math.min(Math.max(size, 1), 100);
        int offset = Math.max(page, 0) * limit;
        return jdbc.queryForList("""
                SELECT id, maintenance_register_id AS "maintenanceId", type, title, body,
                       is_read AS "read", created_at AS "createdAt"
                  FROM notification
                 WHERE employee_id = ? AND (? = false OR is_read = false)
                 ORDER BY created_at DESC LIMIT ? OFFSET ?
                """, employeeId, unreadOnly, limit, offset);
    }

    public long unreadCount(UUID employeeId) {
        Long count = jdbc.queryForObject(
                "SELECT count(*) FROM notification WHERE employee_id = ? AND NOT is_read", Long.class, employeeId);
        return count == null ? 0 : count;
    }

    @Transactional
    public void markRead(UUID employeeId, UUID id) {
        int updated = jdbc.update("""
                UPDATE notification SET is_read = true, read_at = COALESCE(read_at, now())
                 WHERE id = ? AND employee_id = ?
                """, id, employeeId);
        if (updated == 0) {
            throw new ResourceNotFoundException("Notification not found: " + id);
        }
    }

    @Transactional
    public int markAllRead(UUID employeeId) {
        return jdbc.update("""
                UPDATE notification SET is_read = true, read_at = COALESCE(read_at, now())
                 WHERE employee_id = ? AND NOT is_read
                """, employeeId);
    }
}
