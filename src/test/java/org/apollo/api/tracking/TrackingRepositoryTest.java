package org.apollo.api.tracking;

import org.apollo.api.tracking.repository.TrackingRepository;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.UpdateDefinition;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TrackingRepositoryTest {
    private Document event() {
        return new Document("event_id", "event-1").append("user_id", "user-1")
                .append("company_id", 10L).append("event_name", "screen_viewed")
                .append("session_id", "session-1").append("screen", "HomeActivity")
                .append("timestamp", new Date(0)).append("received_at", new Date(1))
                .append("platform", "android").append("source", "android")
                .append("properties", new Document()).append("schema_version", 1);
    }

    @Test
    void usesInsertOnlyUpdateAndAcceptsReplay() {
        MongoOperations mongo = mock(MongoOperations.class);
        Document event = event();
        when(mongo.findOne(any(Query.class), eq(Document.class), eq("tracking_events")))
                .thenReturn(event);
        new TrackingRepository(mongo, "tracking_events").save(event);
        var update = org.mockito.ArgumentCaptor.forClass(UpdateDefinition.class);
        verify(mongo).upsert(any(Query.class), update.capture(), eq("tracking_events"));
        assertEquals(event, update.getValue().getUpdateObject().get("$setOnInsert"));
        assertFalse(update.getValue().getUpdateObject().containsKey("$set"));
    }

    @Test
    void concurrentDuplicateStillVerifiesStoredIdentity() {
        MongoOperations mongo = mock(MongoOperations.class);
        when(mongo.upsert(any(Query.class), any(UpdateDefinition.class), eq("tracking_events")))
                .thenThrow(new DuplicateKeyException("retry race"));
        when(mongo.findOne(any(Query.class), eq(Document.class), eq("tracking_events")))
                .thenReturn(event());
        assertDoesNotThrow(() -> new TrackingRepository(mongo, "tracking_events").save(event()));
    }

    @Test
    void collisionCannotOverwriteAnotherUser() {
        MongoOperations mongo = mock(MongoOperations.class);
        when(mongo.findOne(any(Query.class), eq(Document.class), eq("tracking_events")))
                .thenReturn(event().append("user_id", "other-user"));
        var failure = assertThrows(ResponseStatusException.class,
                () -> new TrackingRepository(mongo, "tracking_events").save(event()));
        assertEquals(409, failure.getStatusCode().value());
    }
}
