package org.apollo.api.tracking;

import com.mongodb.MongoTimeoutException;
import jakarta.validation.Validation;
import org.apollo.api.security.TenantContext;
import org.apollo.api.tracking.dto.TrackingBatchRequest;
import org.apollo.api.tracking.dto.TrackingEventRequest;
import org.apollo.api.tracking.repository.TrackingRepository;
import org.apollo.api.tracking.service.TrackingService;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TrackingServiceTest {
    @Test
    void uxEventsAreAcceptedWithoutCapturingInputContent() {
        TrackingRepository repository = mock(TrackingRepository.class);
        var request = new TrackingBatchRequest(List.of(event("screen_exited",
                Map.of("duration_ms", "18000", "exit_reason", "paused")),
                event("validation_failed", Map.of("form_id", "CompleteMaintenanceActivity",
                        "field", "report", "rule", "required")),
                event("search_completed", Map.of("query_length", "5", "result_count", "0"))));
        assertEquals(3, service(repository, UUID.randomUUID()).accept(request).acceptedEventIds().size());
        verify(repository, times(3)).save(any());
    }

    @Test
    void mongoValidationFailureHasStableDiagnosticCode() {
        TrackingRepository repository = mock(TrackingRepository.class);
        doThrow(new com.mongodb.MongoException(121, "validation failed")).when(repository).save(any());
        var error = assertThrows(ResponseStatusException.class, () ->
                service(repository, UUID.randomUUID()).accept(
                        new TrackingBatchRequest(List.of(event("screen_viewed", Map.of())))));
        assertEquals("MONGO_SCHEMA_REJECTED", error.getReason());
    }
    @SuppressWarnings("unchecked")
    private TrackingService service(TrackingRepository repository, UUID user) {
        ObjectProvider<TrackingRepository> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(repository);
        TenantContext tenant = mock(TenantContext.class);
        when(tenant.getUserId()).thenReturn(user);
        when(tenant.getCompanyId()).thenReturn(10L);
        return new TrackingService(provider, tenant);
    }

    private TrackingEventRequest event(String name, Map<String, String> properties) {
        return new TrackingEventRequest(UUID.randomUUID(), name, UUID.randomUUID(),
                "OsDetailsActivity", Instant.now().minusSeconds(10), "android", properties);
    }

    @Test
    void identityComesFromAuthenticationAndDatesAreBsonDates() {
        TrackingRepository repository = mock(TrackingRepository.class);
        UUID user = UUID.randomUUID();
        TrackingEventRequest event = event("screen_viewed", Map.of());
        var response = service(repository, user).accept(new TrackingBatchRequest(List.of(event)));
        ArgumentCaptor<Document> document = ArgumentCaptor.forClass(Document.class);
        verify(repository).save(document.capture());
        assertEquals(user.toString(), document.getValue().getString("user_id"));
        assertEquals(10L, document.getValue().getLong("company_id"));
        assertInstanceOf(Date.class, document.getValue().get("timestamp"));
        assertInstanceOf(Date.class, document.getValue().get("received_at"));
        assertEquals("android", document.getValue().getString("source"));
        assertEquals(List.of(event.eventId()), response.acceptedEventIds());
    }

    @Test
    void invalidEventRejectsEntireBatchBeforeWriting() {
        TrackingRepository repository = mock(TrackingRepository.class);
        var request = new TrackingBatchRequest(List.of(event("screen_viewed", Map.of()),
                event("work_order_completed", Map.of("work_order_id", "3"))));
        assertThrows(ResponseStatusException.class,
                () -> service(repository, UUID.randomUUID()).accept(request));
        verifyNoInteractions(repository);
    }

    @Test
    void noSensitiveProperties() {
        TrackingRepository repository = mock(TrackingRepository.class);
        var request = new TrackingBatchRequest(List.of(event("action_failed",
                Map.of("token", "secret", "action", "start", "error_type", "NETWORK"))));
        assertThrows(ResponseStatusException.class,
                () -> service(repository, UUID.randomUUID()).accept(request));
        verifyNoInteractions(repository);
    }

    @Test
    void mongoFailureReturns503AndDoesNotAcknowledge() {
        TrackingRepository repository = mock(TrackingRepository.class);
        doThrow(new MongoTimeoutException("unavailable")).when(repository).save(any());
        var error = assertThrows(ResponseStatusException.class, () ->
                service(repository, UUID.randomUUID()).accept(
                        new TrackingBatchRequest(List.of(event("screen_viewed", Map.of())))));
        assertEquals(503, error.getStatusCode().value());
    }

    @Test
    void beanValidationRejectsMissingFieldsAndLargeBatches() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            var invalid = new TrackingEventRequest(null, "", null, "", null, "ios", Map.of());
            assertFalse(validator.validate(new TrackingBatchRequest(List.of(invalid))).isEmpty());
            assertFalse(validator.validate(new TrackingBatchRequest(
                    java.util.Collections.nCopies(51, event("screen_viewed", Map.of())))).isEmpty());
        }
    }

    @Test
    void springDataConnectionFailureReturns503() {
        TrackingRepository repository = mock(TrackingRepository.class);
        doThrow(new org.springframework.dao.DataAccessResourceFailureException("unavailable"))
                .when(repository).save(any());
        var error = assertThrows(ResponseStatusException.class, () ->
                service(repository, UUID.randomUUID()).accept(
                        new TrackingBatchRequest(List.of(event("screen_viewed", Map.of())))));
        assertEquals(503, error.getStatusCode().value());
    }
}
