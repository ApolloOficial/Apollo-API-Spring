package org.apollo.api.recovery;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PasswordRecoveryRepository extends JpaRepository<PasswordRecovery, UUID> {

    Optional<PasswordRecovery> findByResetTokenHashAndConsumedFalse(String resetTokenHash);

    long countByEmployeeIdAndCreatedAtAfter(UUID employeeId, Instant after);

    @Modifying
    @Query("UPDATE PasswordRecovery r SET r.consumed = true WHERE r.employeeId = :employeeId AND r.consumed = false")
    int consumeOpenByEmployee(@Param("employeeId") UUID employeeId);
}
