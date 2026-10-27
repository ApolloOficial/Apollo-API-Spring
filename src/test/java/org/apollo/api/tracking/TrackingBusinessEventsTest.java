package org.apollo.api.tracking;

import org.apollo.api.security.TenantContext;
import org.apollo.api.tracking.repository.TrackingRepository;
import org.apollo.api.tracking.service.TrackingBusinessEvents;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TrackingBusinessEventsTest {
    @TempDir Path directory;

    @SuppressWarnings("unchecked")
    private TrackingBusinessEvents service(Path path) {
        TenantContext tenant = mock(TenantContext.class);
        when(tenant.getUserId()).thenReturn(UUID.randomUUID());
        when(tenant.getCompanyId()).thenReturn(10L);
        return new TrackingBusinessEvents(tenant, mock(ObjectProvider.class), path.toString(), true);
    }

    @Test
    void emitsOnlyAfterCommit() throws Exception {
        TransactionSynchronizationManager.setActualTransactionActive(true);
        TransactionSynchronizationManager.initSynchronization();
        try {
            var service = service(directory);
            service.afterCommit("work_order_started", 3, 1L);
            try (var files = Files.list(directory)) { assertEquals(0, files.count()); }
            for (var sync : TransactionSynchronizationManager.getSynchronizations()) sync.afterCommit();
            try (var files = Files.list(directory)) { assertEquals(1, files.count()); }
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
            TransactionSynchronizationManager.setActualTransactionActive(false);
        }
    }

    @Test
    void rollbackDoesNotSpool() throws Exception {
        TransactionSynchronizationManager.setActualTransactionActive(true);
        TransactionSynchronizationManager.initSynchronization();
        try {
            service(directory).afterCommit("work_order_completed", 3, 1L);
            for (var sync : TransactionSynchronizationManager.getSynchronizations())
                sync.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);
            try (var files = Files.list(directory)) { assertEquals(0, files.count()); }
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
            TransactionSynchronizationManager.setActualTransactionActive(false);
        }
    }

    @Test
    void badSpoolPathCannotThrowIntoBusinessCompletion() throws Exception {
        Path file = directory.resolve("not-a-directory");
        Files.writeString(file, "test");
        TransactionSynchronizationManager.setActualTransactionActive(true);
        TransactionSynchronizationManager.initSynchronization();
        try {
            service(file).afterCommit("work_order_started", 3, 1L);
            for (var sync : TransactionSynchronizationManager.getSynchronizations())
                assertDoesNotThrow(sync::afterCommit);
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
            TransactionSynchronizationManager.setActualTransactionActive(false);
        }
    }
}
