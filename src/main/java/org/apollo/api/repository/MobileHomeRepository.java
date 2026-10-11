package org.apollo.api.repository;

import lombok.RequiredArgsConstructor;
import org.apollo.api.util.DbProcedures;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class MobileHomeRepository {

    private static final String SCOPE_JOINS = """
            FROM maintenance_register m
            JOIN warning w ON w.id = m.warning_id
            LEFT JOIN panel p ON p.id = w.panel_id
            LEFT JOIN string s ON s.id = COALESCE(w.string_id, p.string_id)
            LEFT JOIN inverter i ON i.id = s.inverter_id
            """;

    private static final String SEVERITY_RANK =
            "CASE w.severity WHEN 'CRITICA' THEN 0 WHEN 'ALTA' THEN 1 WHEN 'MEDIA' THEN 2 ELSE 3 END";

    private final JdbcTemplate jdbc;

    public record HeaderRow(String fullName, String unitName, String city, String state, boolean hasPhoto) {
    }

    public record OrderRow(long id, String status, LocalDate dueDate, String severity, String assetCode) {
    }

    public record AttentionRow(long id, String severity, String message, Double healthScore, Long serviceOrderId) {
    }

    public record PendingRow(UUID stringId, String code, int registered, int total) {
    }

    public record Page<T>(List<T> items, int total) {
    }

    static String ordersSql(boolean byTechnician) {
        return "SELECT m.id, m.maintenance_status, m.due_date, w.severity,\n"
                + "       COALESCE(p.serial_number, s.code) AS asset_code, COUNT(*) OVER() AS total\n"
                + SCOPE_JOINS
                + "WHERE i.company_unit_id = ?\n"
                + "  AND m.maintenance_status IN ('ABERTA', 'EM_ANDAMENTO')\n"
                + (byTechnician ? "  AND m.technician_id = ?\n" : "")
                + "ORDER BY " + SEVERITY_RANK + ", m.due_date NULLS LAST, m.id\n"
                + "LIMIT ?";
    }

    static String attentionSql(boolean byTechnician) {
        return "SELECT w.id, w.severity, w.message, h.health_score, m.id AS order_id, COUNT(*) OVER() AS total\n"
                + "FROM warning w\n"
                + "LEFT JOIN panel p ON p.id = w.panel_id\n"
                + "LEFT JOIN string s ON s.id = COALESCE(w.string_id, p.string_id)\n"
                + "LEFT JOIN inverter i ON i.id = s.inverter_id\n"
                + "LEFT JOIN maintenance_register m ON m.warning_id = w.id\n"
                + "LEFT JOIN LATERAL (\n"
                + "    SELECT hh.health_score FROM string_health_history hh\n"
                + "    WHERE hh.string_id = s.id ORDER BY hh.calculated_at DESC LIMIT 1\n"
                + ") h ON TRUE\n"
                + "WHERE i.company_unit_id = ?\n"
                + "  AND w.status IN ('ATIVO', 'RECONHECIDO')\n"
                + (byTechnician ? "  AND m.technician_id = ?\n" : "")
                + "ORDER BY " + SEVERITY_RANK + ", w.generation_dt DESC\n"
                + "LIMIT ?";
    }

    public Optional<HeaderRow> findHeader(UUID employeeId) {
        List<HeaderRow> rows = jdbc.query("""
                SELECT e.full_name, u.name AS unit_name, a.city, a.state,
                       EXISTS (SELECT 1 FROM employee_photo ph WHERE ph.employee_id = e.id) AS has_photo
                FROM employee e
                JOIN company_unit u ON u.id = e.company_unit_id
                JOIN address a ON a.id = u.address_id
                WHERE e.id = ?
                """, (rs, n) -> new HeaderRow(rs.getString("full_name"), rs.getString("unit_name"),
                rs.getString("city"), rs.getString("state"), rs.getBoolean("has_photo")), employeeId);
        return rows.stream().findFirst();
    }

    public Page<OrderRow> findOpenOrders(UUID unitId, UUID technicianId, int limit) {
        List<Object> args = new ArrayList<>();
        args.add(unitId);
        if (technicianId != null) {
            args.add(technicianId);
        }
        args.add(limit);
        int[] total = {0};
        List<OrderRow> rows = jdbc.query(ordersSql(technicianId != null), (rs, n) -> {
            total[0] = rs.getInt("total");
            OffsetDateTime due = rs.getObject("due_date", OffsetDateTime.class);
            return new OrderRow(rs.getLong("id"), rs.getString("maintenance_status"),
                    due == null ? null : due.atZoneSameInstant(DbProcedures.ZONE).toLocalDate(),
                    rs.getString("severity"), rs.getString("asset_code"));
        }, args.toArray());
        return new Page<>(rows, total[0]);
    }

    public Page<AttentionRow> findAttention(UUID unitId, UUID technicianId, int limit) {
        List<Object> args = new ArrayList<>();
        args.add(unitId);
        if (technicianId != null) {
            args.add(technicianId);
        }
        args.add(limit);
        int[] total = {0};
        List<AttentionRow> rows = jdbc.query(attentionSql(technicianId != null), (rs, n) -> {
            total[0] = rs.getInt("total");
            double score = rs.getDouble("health_score");
            Double health = rs.wasNull() ? null : score;
            long orderId = rs.getLong("order_id");
            Long order = rs.wasNull() ? null : orderId;
            return new AttentionRow(rs.getLong("id"), rs.getString("severity"), rs.getString("message"), health, order);
        }, args.toArray());
        return new Page<>(rows, total[0]);
    }

    public Page<PendingRow> findPendingRegistrations(UUID unitId, int limit) {
        int[] total = {0};
        List<PendingRow> rows = jdbc.query("""
                SELECT s.id, s.code, s.purchased_qty,
                       COUNT(p.id) FILTER (WHERE p.operating_stats <> 'EM_ESTOQUE') AS registered,
                       COUNT(*) OVER() AS total
                FROM string s
                JOIN inverter i ON i.id = s.inverter_id
                LEFT JOIN panel p ON p.string_id = s.id
                WHERE i.company_unit_id = ? AND s.status = 'EM_ESTOQUE'
                GROUP BY s.id, s.code, s.purchased_qty, s.created_at
                HAVING COUNT(p.id) FILTER (WHERE p.operating_stats <> 'EM_ESTOQUE') < s.purchased_qty
                ORDER BY s.created_at, s.id
                LIMIT ?
                """, (rs, n) -> {
            total[0] = rs.getInt("total");
            return new PendingRow(rs.getObject("id", UUID.class), rs.getString("code"),
                    rs.getInt("registered"), rs.getInt("purchased_qty"));
        }, unitId, limit);
        return new Page<>(rows, total[0]);
    }
}
