package org.apollo.api.tracking;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClients;
import org.apollo.api.tracking.service.TrackingFailureCode;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.fail;

/** Read-only Atlas check. Never prints the URI or document contents. */
@EnabledIfEnvironmentVariable(named = "TRACKING_DIAGNOSTICS", matches = "true")
class TrackingAtlasDiagnosticsTest {
    @Test void readOnlyConnectivity() {
        ch.qos.logback.classic.Logger driver = (ch.qos.logback.classic.Logger)
                org.slf4j.LoggerFactory.getLogger("org.mongodb.driver");
        driver.setLevel(ch.qos.logback.classic.Level.OFF);
        String uri = System.getenv("TRACKING_MONGO_URI");
        if (uri == null) uri = System.getenv("MONGODB_URI");
        if (uri == null) fail("MONGO_URI_NOT_CONFIGURED");
        MongoClientSettings settings;
        try {
            settings = MongoClientSettings.builder().applyConnectionString(new ConnectionString(uri))
                .applyToClusterSettings(b -> b.serverSelectionTimeout(10, TimeUnit.SECONDS))
                .applyToSocketSettings(b -> b.connectTimeout(5, TimeUnit.SECONDS)
                        .readTimeout(5, TimeUnit.SECONDS)).build();
        } catch (RuntimeException failure) {
            fail(TrackingFailureCode.classify(failure));
            return;
        }
        try (var client = MongoClients.create(settings)) {
            var db = client.getDatabase("apollo_analytics");
            db.runCommand(new Document("ping", 1));
            // A find with projection checks application read privileges without printing records.
            db.getCollection("tracking_events").find().projection(new Document("_id", 1)).limit(1).first();
            System.out.println("TRACKING_DIAGNOSTIC: ATLAS_CONNECTED_AND_FIND_AUTHORIZED");
            try {
                Document info = db.listCollections().filter(new Document("name", "tracking_events")).first();
                if (info == null) {
                    System.out.println("TRACKING_DIAGNOSTIC: COLLECTION_NOT_FOUND");
                } else {
                    Document options = info.get("options", Document.class);
                    System.out.println("TRACKING_DIAGNOSTIC: COLLECTION_EXISTS validator="
                            + (options != null && options.containsKey("validator")));
                }
            } catch (RuntimeException failure) {
                System.out.println("TRACKING_DIAGNOSTIC: SCHEMA_INSPECTION_" + TrackingFailureCode.classify(failure));
            }
        } catch (RuntimeException failure) {
            fail(TrackingFailureCode.classify(failure));
        }
    }
}
