package org.apollo.api.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MobileHomeRepositoryTest {

    private static long placeholders(String sql) {
        return sql.chars().filter(c -> c == '?').count();
    }

    private static void assertWellFormed(String sql) {
        assertFalse(sql.matches("(?s).*\\?[A-Za-z].*"), "parameter glued to the next keyword: " + sql);
        assertFalse(sql.contains("BYCASE"), "missing space after ORDER BY: " + sql);
        assertTrue(sql.contains("\nORDER BY CASE w.severity"), "ORDER BY must start its own line: " + sql);
        assertTrue(sql.endsWith("\nLIMIT ?"), "LIMIT must follow on its own line: " + sql);
    }

    @Test
    void ordersSqlWithoutTechnicianHasUnitAndLimitPlaceholders() {
        String sql = MobileHomeRepository.ordersSql(false);

        assertWellFormed(sql);
        assertEquals(2, placeholders(sql));
        assertFalse(sql.contains("technician_id"));
    }

    @Test
    void ordersSqlWithTechnicianAddsOnePlaceholderBeforeOrdering() {
        String sql = MobileHomeRepository.ordersSql(true);

        assertWellFormed(sql);
        assertEquals(3, placeholders(sql));
        assertTrue(sql.indexOf("m.technician_id = ?") < sql.lastIndexOf("ORDER BY"));
    }

    @Test
    void attentionSqlWithoutTechnicianHasUnitAndLimitPlaceholders() {
        String sql = MobileHomeRepository.attentionSql(false);

        assertWellFormed(sql);
        assertEquals(2, placeholders(sql));
        assertFalse(sql.contains("technician_id"));
    }

    @Test
    void attentionSqlWithTechnicianAddsOnePlaceholderBeforeOrdering() {
        String sql = MobileHomeRepository.attentionSql(true);

        assertWellFormed(sql);
        assertEquals(3, placeholders(sql));
        assertTrue(sql.indexOf("m.technician_id = ?") < sql.lastIndexOf("ORDER BY"));
    }
}
