package org.apollo.api.tracking.service;

import com.mongodb.MongoException;
import org.apollo.api.security.TenantContext;
import org.apollo.api.tracking.dto.TrackingBatchRequest;
import org.apollo.api.tracking.dto.TrackingBatchResponse;
import org.apollo.api.tracking.dto.TrackingEventRequest;
import org.apollo.api.tracking.model.TrackingEventDocument;
import org.apollo.api.tracking.repository.TrackingRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.dao.DataAccessException;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class TrackingService {
    private static final Set<String> CLIENT_EVENTS = TrackingCatalog.EVENTS;
    private static final Set<String> KEYS = TrackingCatalog.KEYS;
    private final ObjectProvider<TrackingRepository> repositories;
    private final TenantContext tenant;

    public TrackingService(ObjectProvider<TrackingRepository> repositories, TenantContext tenant) {
        this.repositories = repositories;
        this.tenant = tenant;
    }

    public TrackingBatchResponse accept(TrackingBatchRequest batch) {
        TrackingRepository repository = repositories.getIfAvailable();
        if (repository == null) throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE, "TRACKING_DISABLED");
        Instant now = Instant.now();
        batch.events().forEach(e -> validate(e, now));
        List<TrackingEventDocument> documents = batch.events().stream().map(e ->
                new TrackingEventDocument(e.eventId(), e.eventName(), tenant.getUserId(),
                        tenant.getCompanyId(), e.sessionId(), e.screen(), e.timestamp(), now,
                        "android", "android", Map.copyOf(e.properties()))).toList();
        try {
            documents.forEach(e -> repository.save(e.toBson()));
        } catch (MongoException | DataAccessException | IllegalStateException e) {
            String code = TrackingFailureCode.classify(e);
            org.slf4j.LoggerFactory.getLogger(TrackingService.class)
                    .warn("Tracking batch rejected: code={}, count={}", code, documents.size());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, code);
        }
        return new TrackingBatchResponse(batch.events().stream()
                .map(TrackingEventRequest::eventId).toList());
    }

    private void validate(TrackingEventRequest e, Instant now) {
        boolean invalid = !CLIENT_EVENTS.contains(e.eventName())
                || e.timestamp().isBefore(Instant.parse("2020-01-01T00:00:00Z"))
                || e.timestamp().isAfter(now.plusSeconds(300));
        if (invalid) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid event");
        for (var entry : e.properties().entrySet()) {
            String value = entry.getValue();
            if (!KEYS.contains(entry.getKey()) || value == null || value.length() > 128
                    || !value.matches("[A-Za-z0-9_.:/ -]*")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid properties");
            }
        }
        if (e.eventName().startsWith("work_order_") || e.eventName().equals("report_started")) {
            if (!e.properties().getOrDefault("work_order_id", "").matches("[1-9][0-9]{0,18}")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing order ID");
            }
        }
        if (e.eventName().equals("action_failed")
                && (!e.properties().containsKey("action") || !e.properties().containsKey("error_type"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing failure category");
        }
    }
}
