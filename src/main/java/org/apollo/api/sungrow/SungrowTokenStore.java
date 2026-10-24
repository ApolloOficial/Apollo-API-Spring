package org.apollo.api.sungrow;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.function.Supplier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Component
@ConditionalOnSungrowLive
public class SungrowTokenStore {
    private final JdbcTemplate jdbc;
    private final PlatformTransactionManager transactionManager;

    public SungrowTokenStore(JdbcTemplate jdbc, PlatformTransactionManager transactionManager) {
        this.jdbc = jdbc;
        this.transactionManager = transactionManager;
    }

    public Optional<SungrowTokens> find() {
        return jdbc.query("""
                        SELECT access_token, refresh_token, expires_at, authorized_plants
                          FROM sungrow_credential WHERE id = 1
                        """,
                (rs, rowNum) -> new SungrowTokens(rs.getString("access_token"), rs.getString("refresh_token"),
                        rs.getTimestamp("expires_at").toInstant(), rs.getString("authorized_plants")))
                .stream().findFirst();
    }

    public void save(SungrowTokens tokens) {
        jdbc.update("""
                INSERT INTO sungrow_credential (id, access_token, refresh_token, expires_at, authorized_plants, updated_at)
                VALUES (1, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                ON CONFLICT (id) DO UPDATE
                   SET access_token = EXCLUDED.access_token, refresh_token = EXCLUDED.refresh_token,
                       expires_at = EXCLUDED.expires_at, authorized_plants = EXCLUDED.authorized_plants,
                       updated_at = CURRENT_TIMESTAMP
                """,
                tokens.accessToken(), tokens.refreshToken(), Timestamp.from(tokens.expiresAt()),
                tokens.authorizedPlants());
    }

    public <T> T withLock(Supplier<T> action) {
        return new TransactionTemplate(transactionManager).execute(status -> {
            jdbc.queryForList("SELECT id FROM sungrow_credential WHERE id = 1 FOR UPDATE");
            return action.get();
        });
    }
}
