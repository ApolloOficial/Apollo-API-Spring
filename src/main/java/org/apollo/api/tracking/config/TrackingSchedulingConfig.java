package org.apollo.api.tracking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration(proxyBeanMethods = false)
public class TrackingSchedulingConfig {
    @Bean(name = "trackingTaskScheduler", defaultCandidate = false)
    ThreadPoolTaskScheduler trackingTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("tracking-flush-");
        scheduler.setRemoveOnCancelPolicy(true);
        return scheduler;
    }
}
