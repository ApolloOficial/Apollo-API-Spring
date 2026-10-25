package org.apollo.api.tracking;

import com.mongodb.client.MongoClients;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import org.apollo.api.tracking.model.TrackingEventDocument;
import org.apollo.api.tracking.repository.TrackingRepository;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class TrackingRepositoryIntegrationTest {
    @Test
    void persistsOnceAndRejectsConflictingOwner() {
        String uri = System.getenv("TRACKING_TEST_MONGO_URI");
        assumeTrue(uri != null && !uri.isBlank(), "Mongo de teste não configurado");
        try (var client = MongoClients.create(uri)) {
            var collection = client.getDatabase("apollo_analytics_test")
                    .getCollection("tracking_test_" + UUID.randomUUID().toString().replace("-", ""));
            try {
                collection.createIndex(Indexes.ascending("event_id"), new IndexOptions().unique(true));
                var repository = new TrackingRepository(new MongoTemplate(client, "apollo_analytics_test"),
                        collection.getNamespace().getCollectionName());
                UUID id = UUID.randomUUID();
                UUID user = UUID.randomUUID();
                UUID session = UUID.randomUUID();
                Instant at = Instant.ofEpochMilli(System.currentTimeMillis() - 1000);
                var event = new TrackingEventDocument(id, "screen_viewed", user, 10L,
                        session, "HomeActivity", at, at, "android", "android", Map.of());
                repository.save(event.toBson());
                var retry = event.toBson();
                retry.put("received_at", new Date());
                repository.save(retry);
                assertEquals(1, collection.countDocuments());
                assertEquals(Date.from(at), collection.find().first().getDate("received_at"));
                var conflict = event.toBson();
                conflict.put("user_id", UUID.randomUUID().toString());
                assertThrows(ResponseStatusException.class, () -> repository.save(conflict));
                assertEquals(user.toString(), collection.find().first().getString("user_id"));
            } finally {
                collection.drop();
            }
        }
    }
}
