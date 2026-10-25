package org.apollo.api.tracking.service;

import org.apollo.api.security.TenantContext;
import org.apollo.api.tracking.model.TrackingEventDocument;
import org.apollo.api.tracking.repository.TrackingRepository;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class TrackingBusinessEvents {
    private static final Logger log = LoggerFactory.getLogger(TrackingBusinessEvents.class);
    private final TenantContext tenant;
    private final ObjectProvider<TrackingRepository> repositories;
    private final Path directory;
    private final boolean enabled;

    public TrackingBusinessEvents(TenantContext tenant, ObjectProvider<TrackingRepository> repositories,
                                  @Value("${apollo.tracking.spool-dir:./var/tracking-spool}") String directory,
                                  @Value("${apollo.tracking.enabled:false}") boolean enabled) {
        this.tenant = tenant;
        this.repositories = repositories;
        this.directory = Path.of(directory).toAbsolutePath().normalize();
        this.enabled = enabled;
    }

    public void afterCommit(String name, long orderId, Long typeId) {
        if (!enabled) return;
        try {
            UUID user = tenant.getUserId();
            Long company = tenant.getCompanyId();
            var attributes = RequestContextHolder.getRequestAttributes();
            var request = attributes instanceof ServletRequestAttributes a ? a.getRequest() : null;
            UUID session = UUID.randomUUID();
            String screen = "MaintenanceRegisterService";
            String platform = "api";
            if (request != null) {
                try {
                    session = UUID.fromString(request.getHeader("X-Tracking-Session"));
                    platform = "android";
                } catch (IllegalArgumentException | NullPointerException ignored) { }
                String value = request.getHeader("X-Tracking-Screen");
                if (value != null && value.matches("[A-Za-z][A-Za-z0-9_]{0,79}")) screen = value;
            }
            UUID eventId = UUID.nameUUIDFromBytes((company + ":" + orderId + ":" + name)
                    .getBytes(StandardCharsets.UTF_8));
            Instant now = Instant.now();
            Document document = new TrackingEventDocument(eventId, name, user, company, session,
                    screen, now, now, platform, "api", Map.of("work_order_id", Long.toString(orderId),
                    "maintenance_type_id", String.valueOf(typeId))).toBson();
            if (!TransactionSynchronizationManager.isActualTransactionActive()
                    || !TransactionSynchronizationManager.isSynchronizationActive()) {
                log.error("tracking_missing_transaction event_id={}", eventId);
                return;
            }
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() { spool(document); }
            });
        } catch (RuntimeException e) {
            log.error("tracking_capture_failed type={}", e.getClass().getSimpleName());
        }
    }

    private void spool(Document document) {
        Path temporary = null;
        try {
            Files.createDirectories(directory);
            Path target = directory.resolve(document.getString("event_id") + ".json");
            if (Files.exists(target)) return;
            temporary = Files.createTempFile(directory, "pending-", ".tmp");
            Files.writeString(temporary, document.toJson(), StandardCharsets.UTF_8);
            try (FileChannel channel = FileChannel.open(temporary, StandardOpenOption.WRITE)) {
                channel.force(true);
            }
            Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE);
        } catch (Exception e) {
            log.error("tracking_spool_failed event_id={} type={}",
                    document.getString("event_id"), e.getClass().getSimpleName());
        } finally {
            if (temporary != null) {
                try { Files.deleteIfExists(temporary); } catch (Exception ignored) { }
            }
        }
    }

    @Scheduled(fixedDelayString = "${apollo.tracking.flush-delay-ms:10000}", scheduler = "trackingTaskScheduler")
    public void flush() {
        if (!enabled || !Files.isDirectory(directory)) return;
        TrackingRepository repository = repositories.getIfAvailable();
        if (repository == null) return;
        try (var files = Files.list(directory)) {
            List<Path> pending = files.filter(p -> p.getFileName().toString().endsWith(".json"))
                    .sorted().limit(100).toList();
            for (Path path : pending) {
                Document document = Document.parse(Files.readString(path, StandardCharsets.UTF_8));
                repository.save(document);
                Files.delete(path);
            }
        } catch (Exception e) {
            log.warn("tracking_flush_failed code={} type={}",
                    TrackingFailureCode.classify(e), e.getClass().getSimpleName());
        }
    }
}
