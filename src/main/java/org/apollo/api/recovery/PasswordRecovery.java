package org.apollo.api.recovery;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "password_recovery")
@Getter
@Setter
@NoArgsConstructor
public class PasswordRecovery {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "employee_id")
    private UUID employeeId;

    @Column(name = "code_hash", length = 100)
    private String codeHash;

    @Column(name = "code_expires_at")
    private Instant codeExpiresAt;

    @Column(name = "attempts", nullable = false)
    private int attempts;

    @Column(name = "send_count", nullable = false)
    private int sendCount;

    @Column(name = "last_sent_at")
    private Instant lastSentAt;

    @Column(name = "reset_token_hash", length = 64)
    private String resetTokenHash;

    @Column(name = "reset_expires_at")
    private Instant resetExpiresAt;

    @Column(name = "consumed", nullable = false)
    private boolean consumed;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}
