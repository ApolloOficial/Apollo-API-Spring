package org.apollo.api.util;

import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.RelocationCandidateDTO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

/**
 * Chamadas as procedures/funcoes do banco (Script_SQL_Apollo_2ano.sql).
 * <p>
 * As regras de negocio do 2o ano (quem pode abrir OS, baixa de estoque, mudanca de status
 * de string/placa...) moram no banco, em procedures e triggers. A API so chama essas
 * procedures — assim o fluxo e sempre o mesmo, venha do web ou do mobile.
 * Erros de regra (RAISE EXCEPTION) sao convertidos em 400 pelo GlobalExceptionHandler.
 */
@Component
@RequiredArgsConstructor
public class DbProcedures {

    /** Fuso usado para datas "sem fuso" que chegam da API. */
    public static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");

    private final JdbcTemplate jdbc;

    public static LocalDate today() {
        return LocalDate.now(ZONE);
    }

    public static LocalDateTime now() {
        return LocalDateTime.now(ZONE);
    }

    private static OffsetDateTime toOffset(LocalDateTime value) {
        return value == null ? null : value.atZone(ZONE).toOffsetDateTime();
    }

    // ---------------------------------------------------------------- inversor / placas

    /** pr_register_inverter: cria o inversor, as strings e as placas de uma vez (operador). */
    public UUID registerInverter(UUID operatorId, String payloadJson) {
        return jdbc.queryForObject("CALL pr_register_inverter(?::uuid, ?::jsonb, NULL::uuid)",
                UUID.class, operatorId, payloadJson);
    }

    /** pr_activate_panel: ativa a placa pelo codigo de barras (tecnico). */
    public void activatePanel(String barcode, UUID technicianId, LocalDate installationDt) {
        jdbc.update("CALL pr_activate_panel(?::varchar, ?::uuid, ?::date)", barcode, technicianId, installationDt);
    }

    /** pr_deactivate_panel: desativa a placa (tecnico). */
    public void deactivatePanel(String barcode, UUID technicianId, String reason) {
        jdbc.update("CALL pr_deactivate_panel(?::varchar, ?::uuid, ?::text)", barcode, technicianId, reason);
    }

    /** pr_report_panel_issue: relato de campo que gera um alerta de placa. Retorna o id do alerta. */
    public Long reportPanelIssue(String barcode, UUID technicianId, String type, String severity, String message) {
        return jdbc.queryForObject(
                "CALL pr_report_panel_issue(?::varchar, ?::uuid, ?::varchar, ?::varchar, ?::text, NULL::bigint)",
                Long.class, barcode, technicianId, type, severity, message);
    }

    // ---------------------------------------------------------------- ordens de servico

    /** pr_open_maintenance: abre a OS a partir de um alerta ATIVO (operador). Retorna o id da OS. */
    public Long openMaintenance(Long warningId, UUID operatorId, UUID technicianId, Long maintenanceTypeId,
                                LocalDateTime dueDate, BigDecimal estimatedCost, Long parentMaintenanceId) {
        return jdbc.queryForObject(
                "CALL pr_open_maintenance(?::bigint, ?::uuid, ?::uuid, ?::bigint, ?::timestamptz, ?::numeric, ?::bigint, NULL::bigint)",
                Long.class, warningId, operatorId, technicianId, maintenanceTypeId, toOffset(dueDate),
                estimatedCost, parentMaintenanceId);
    }

    public void startMaintenance(Long maintenanceId, UUID technicianId) {
        jdbc.update("CALL pr_start_maintenance(?::bigint, ?::uuid)", maintenanceId, technicianId);
    }

    public void completeMaintenance(Long maintenanceId, UUID technicianId, String report, BigDecimal laborCost) {
        jdbc.update("CALL pr_complete_maintenance(?::bigint, ?::uuid, ?::text, ?::numeric)",
                maintenanceId, technicianId, report, laborCost);
    }

    public void cancelMaintenance(Long maintenanceId, UUID operatorId, String reason) {
        jdbc.update("CALL pr_cancel_maintenance(?::bigint, ?::uuid, ?::text)", maintenanceId, operatorId, reason);
    }

    /** O trigger trg_consume_maintenance_part baixa o estoque da filial e preenche o custo. */
    public void addMaintenancePart(Long maintenanceId, Long partId, Integer quantity) {
        jdbc.update("INSERT INTO maintenance_part (maintenance_id, part_id, quantity) VALUES (?, ?, ?)",
                maintenanceId, partId, quantity);
    }

    // ---------------------------------------------------------------- saude das strings

    /** pr_recalculate_string_health: recalcula o indice de saude (e gera/resolve alertas de eficiencia). */
    public void recalculateHealth(UUID companyUnitId, int windowDays, BigDecimal threshold) {
        jdbc.update("CALL pr_recalculate_string_health(?::uuid, ?::integer, ?::numeric, CURRENT_TIMESTAMP)",
                companyUnitId, windowDays, threshold);
    }

    /** fn_relocation_candidates: strings da empresa com saude abaixo do limiar. */
    public List<RelocationCandidateDTO> relocationCandidates(Long companyId, BigDecimal threshold) {
        return jdbc.query("SELECT * FROM fn_relocation_candidates(?::bigint, ?::numeric)",
                (rs, rowNum) -> {
                    Timestamp calculatedAt = rs.getTimestamp("health_calculated_at");
                    return new RelocationCandidateDTO(
                            rs.getObject("string_id", UUID.class),
                            rs.getString("string_code"),
                            rs.getString("inverter_code"),
                            rs.getObject("company_unit_id", UUID.class),
                            rs.getString("unit_name"),
                            rs.getString("manufacturer"),
                            rs.getString("model"),
                            rs.getLong("installed_panels"),
                            rs.getBigDecimal("nominal_power_wp"),
                            rs.getBigDecimal("health_score"),
                            calculatedAt == null ? null
                                    : calculatedAt.toInstant().atZone(ZONE).toLocalDateTime(),
                            rs.getLong("ranking"));
                },
                companyId, threshold);
    }

    // ---------------------------------------------------------------- auditoria de acesso

    /** pr_register_access: registra login (SUCESSO/FALHA) na tabela access_log. */
    public void registerAccess(String email, UUID sessionId, String ipAddress, String status) {
        jdbc.update("CALL pr_register_access(?::varchar, ?::uuid, ?::inet, ?::varchar)",
                email, sessionId, ipAddress, status);
    }
}
