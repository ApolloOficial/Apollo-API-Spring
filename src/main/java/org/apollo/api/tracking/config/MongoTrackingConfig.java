package org.apollo.api.tracking.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableScheduling
public class MongoTrackingConfig {
    @Bean
    org.springframework.boot.ApplicationRunner trackingStatus(
            @Value("${apollo.tracking.enabled:false}") boolean enabled,
            @Value("${apollo.tracking.database}") String database,
            @Value("${apollo.tracking.collection:tracking_events}") String collection) {
        return args -> org.slf4j.LoggerFactory.getLogger(MongoTrackingConfig.class)
                .info("Tracking configuration: enabled={}, database={}, collection={}",
                        enabled, database, collection);
    }

    @Bean(destroyMethod = "close")
    @ConditionalOnProperty(name = "apollo.tracking.enabled", havingValue = "true")
    MongoClient trackingMongoClient(@Value("${apollo.tracking.mongo-uri}") String uri) {
        return MongoClients.create(MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(uri))
                .applyToClusterSettings(b -> b.serverSelectionTimeout(3, TimeUnit.SECONDS))
                .applyToSocketSettings(b -> b.connectTimeout(3, TimeUnit.SECONDS)
                        .readTimeout(5, TimeUnit.SECONDS))
                .writeConcern(WriteConcern.MAJORITY.withWTimeout(5, TimeUnit.SECONDS))
                .retryWrites(true).build());
    }

    @Bean
    @ConditionalOnProperty(name = "apollo.tracking.enabled", havingValue = "true")
    MongoTemplate trackingMongoTemplate(MongoClient trackingMongoClient,
                                       @Value("${apollo.tracking.database}") String database) {
        return new MongoTemplate(trackingMongoClient, database);
    }
}
