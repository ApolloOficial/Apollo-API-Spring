package org.apollo.api.notification;

import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Envia o push das notificacoes PENDING (criadas pelo trigger ao abrir OS). Sem credencial do
 * Firebase nada e enviado e as notificacoes seguem visiveis no inbox. SKIP LOCKED permite
 * mais de uma instancia sem enviar em duplicidade.
 */
@Slf4j
@Component
public class PushDispatcher {

    private final JdbcTemplate jdbc;
    private final ObjectProvider<PushSender> senderProvider;

    public PushDispatcher(JdbcTemplate jdbc, ObjectProvider<PushSender> senderProvider) {
        this.jdbc = jdbc;
        this.senderProvider = senderProvider;
    }

    @Scheduled(fixedDelayString = "${app.push.poll-ms:15000}")
    @Transactional
    public void dispatch() {
        PushSender sender = senderProvider.getIfAvailable();
        if (sender == null) {
            return;
        }
        List<Map<String, Object>> pending = jdbc.queryForList("""
                SELECT id, employee_id, maintenance_register_id, title, body FROM notification
                 WHERE push_status = 'PENDING' ORDER BY created_at LIMIT 50 FOR UPDATE SKIP LOCKED
                """);
        for (Map<String, Object> n : pending) {
            List<Map<String, Object>> tokens = jdbc.queryForList(
                    "SELECT id, fcm_token FROM device_push_token WHERE employee_id = ? AND is_active",
                    n.get("employee_id"));
            if (tokens.isEmpty()) {
                finish(n, "SKIPPED", "no active device token");
                continue;
            }
            Map<String, String> data = new java.util.HashMap<>();
            data.put("notificationId", String.valueOf(n.get("id")));
            if (n.get("maintenance_register_id") != null) {
                data.put("maintenanceId", String.valueOf(n.get("maintenance_register_id")));
            }
            int sent = 0;
            for (Map<String, Object> t : tokens) {
                PushSender.Result result = sender.send((String) t.get("fcm_token"), (String) n.get("title"),
                        (String) n.get("body"), data);
                if (result == PushSender.Result.SENT) {
                    sent++;
                } else if (result == PushSender.Result.INVALID_TOKEN) {
                    jdbc.update("UPDATE device_push_token SET is_active = false WHERE id = ?", t.get("id"));
                }
            }
            if (sent > 0) {
                finish(n, "SENT", null);
            } else {
                finish(n, "FAILED", "no token accepted the message");
            }
        }
    }

    private void finish(Map<String, Object> n, String status, String error) {
        jdbc.update("UPDATE notification SET push_status = ?, push_error = ?, "
                + "push_sent_at = CASE WHEN ? = 'SENT' THEN now() END WHERE id = ?",
                status, error, status, n.get("id"));
    }
}
