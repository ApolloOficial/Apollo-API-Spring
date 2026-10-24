package org.apollo.api.sungrow;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Quais inversores ler: marca contendo o texto configurado (apollo.sungrow.brand), inversor ATIVO, modelo
 * que mede corrente por string e pelo menos uma string ATIVA/em MANUTENCAO. O numero de serie do
 * inversor e a chave de ligacao com a Sungrow (device_sn).
 */
@Repository
@ConditionalOnProperty(prefix = "apollo.sungrow", name = "enabled", havingValue = "true")
public class SungrowTargetRepository {

    private final JdbcTemplate jdbc;

    public SungrowTargetRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<SungrowTarget> findTargets(String brand) {
        Map<String, SungrowTarget> bySerial = new LinkedHashMap<>();
        jdbc.query("""
                SELECT i.serial_number, m.mppt_count, m.strings_per_mppt, s.mppt_number, s.entry_number
                  FROM inverter i
                  JOIN inverter_model m ON m.id = i.inverter_model_id
                  JOIN string s ON s.inverter_id = i.id
                 WHERE i.status = 'ATIVO'
                   AND m.string_current_monitoring
                   AND s.status IN ('ATIVA', 'MANUTENCAO')
                   AND lower(m.brand) LIKE '%' || lower(?) || '%'
                 ORDER BY i.serial_number, s.mppt_number, s.entry_number
                """,
                rs -> {
                    String serial = rs.getString("serial_number");
                    SungrowTarget target = bySerial.get(serial);
                    if (target == null) {
                        target = new SungrowTarget(serial, rs.getInt("mppt_count"), rs.getInt("strings_per_mppt"),
                                new HashSet<>());
                        bySerial.put(serial, target);
                    }
                    target.activeSlots().add(new SungrowPointMapper.StringSlot(rs.getInt("mppt_number"),
                            rs.getInt("entry_number")));
                },
                brand);
        return new ArrayList<>(bySerial.values());
    }
}
