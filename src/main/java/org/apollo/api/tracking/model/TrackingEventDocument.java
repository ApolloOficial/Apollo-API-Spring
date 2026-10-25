package org.apollo.api.tracking.model;

import org.bson.Document;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

public record TrackingEventDocument(UUID eventId, String eventName, UUID userId,
                                    Long companyId, UUID sessionId, String screen, Instant timestamp,
                                    Instant receivedAt, String platform, String source, Map<String, String> properties) {

    public Document toBson() {
        return new Document("event_id", eventId.toString())
                .append("event_name", eventName)
                .append("user_id", userId.toString())
                .append("company_id", companyId)
                .append("session_id", sessionId.toString())
                .append("screen", screen)
                .append("timestamp", Date.from(timestamp))
                .append("received_at", Date.from(receivedAt))
                .append("platform", platform).append("source", source)
                .append("schema_version", 1)
                .append("properties", new Document(new java.util.HashMap<String, Object>(properties)));
    }
}