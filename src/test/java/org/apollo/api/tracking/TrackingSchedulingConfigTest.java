package org.apollo.api.tracking;

import org.apollo.api.tracking.config.TrackingSchedulingConfig;
import org.apollo.api.tracking.service.TrackingBusinessEvents;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class TrackingSchedulingConfigTest {
    @Test
    void mongoWaitDoesNotBlockOtherScheduledJobs() throws Exception {
        try (var context = new AnnotationConfigApplicationContext(
                TrackingSchedulingConfig.class, OtherJobsConfig.class)) {
            var tracking = context.getBean("trackingTaskScheduler", ThreadPoolTaskScheduler.class);
            var main = context.getBean("taskScheduler", ThreadPoolTaskScheduler.class);
            var waitingForMongo = new CountDownLatch(1);
            var releaseMongo = new CountDownLatch(1);
            var otherJobCompleted = new CountDownLatch(1);
            try {
                tracking.schedule(() -> {
                    waitingForMongo.countDown();
                    try {
                        releaseMongo.await(10, TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }, Instant.now());
                assertTrue(waitingForMongo.await(5, TimeUnit.SECONDS));
                main.schedule(otherJobCompleted::countDown, Instant.now());
                assertTrue(otherJobCompleted.await(5, TimeUnit.SECONDS),
                        "Other jobs must remain independent of Mongo latency");
                assertNotSame(main, tracking);
            } finally {
                releaseMongo.countDown();
            }
        }
    }

    @Test
    void flushExplicitlyUsesItsDedicatedScheduler() throws Exception {
        Scheduled annotation = TrackingBusinessEvents.class.getMethod("flush")
                .getAnnotation(Scheduled.class);
        assertEquals("trackingTaskScheduler", annotation.scheduler());
    }

    @Configuration(proxyBeanMethods = false)
    static class OtherJobsConfig {
        @Bean
        ThreadPoolTaskScheduler taskScheduler() {
            var scheduler = new ThreadPoolTaskScheduler();
            scheduler.setPoolSize(1);
            scheduler.setThreadNamePrefix("other-jobs-");
            return scheduler;
        }
    }
}
