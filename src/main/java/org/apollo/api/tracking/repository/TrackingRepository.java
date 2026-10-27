package org.apollo.api.tracking.repository;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;
import java.util.Objects;

@Repository
@ConditionalOnProperty(name = "apollo.tracking.enabled", havingValue = "true")
public class TrackingRepository {
    private final MongoOperations mongo;
    private final String collection;

    public TrackingRepository(@Qualifier("trackingMongoTemplate") MongoOperations mongo,
                              @Value("${apollo.tracking.collection:tracking_events}") String collection) {
        this.mongo = mongo;
        this.collection = collection;
    }

    public void save(Document event) {
        Query filter = Query.query(Criteria.where("event_id").is(event.getString("event_id")));
        Update insertOnly = new Update();
        event.forEach(insertOnly::setOnInsert);
        try {
            mongo.upsert(filter, insertOnly, collection);
        } catch (DuplicateKeyException e) {
            // Concurrent retries can race on the unique event_id index.
        }
        Document stored = mongo.findOne(filter, Document.class, collection);
        if (stored == null) throw new IllegalStateException("Tracking write not acknowledged");
        for (String key : List.of("user_id", "company_id", "event_name", "session_id",
                "screen", "timestamp", "platform", "source", "properties", "schema_version")) {
            if (!Objects.equals(stored.get(key), event.get(key))) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Event ID conflict");
            }
        }
    }
}
