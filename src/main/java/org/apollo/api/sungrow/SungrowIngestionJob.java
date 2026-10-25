package org.apollo.api.sungrow;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apollo.api.util.DbProcedures;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataAccessException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "apollo.sungrow", name = "enabled", havingValue = "true")
public class SungrowIngestionJob {
    private static final Logger log = LoggerFactory.getLogger(SungrowIngestionJob.class);

    public record Result(int inverters, int readingsSaved, int readingsFailed) {
        static final Result EMPTY = new Result(0, 0, 0);
    }

    private final SungrowProperties properties;
    private final SungrowRealTimeSource source;
    private final SungrowTargetRepository targets;
    private final DbProcedures db;
    private boolean warnedNotAuthorized;

    public SungrowIngestionJob(SungrowProperties properties, SungrowRealTimeSource source,
                               SungrowTargetRepository targets, DbProcedures db) {
        this.properties = properties;
        this.source = source;
        this.targets = targets;
        this.db = db;
    }

    @Scheduled(initialDelayString = "${apollo.sungrow.initial-delay-seconds:30}",
            fixedDelayString = "${apollo.sungrow.poll-interval-seconds:300}", timeUnit = TimeUnit.SECONDS)
    public void poll() {
        try {
            Result result = ingestOnce();
            if (result.inverters() > 0) {
                log.info("Sungrow ingestion: {} inverter(s), {} reading(s) saved, {} failed",
                        result.inverters(), result.readingsSaved(), result.readingsFailed());
            }
        } catch (RuntimeException exception) {
            log.error("Sungrow ingestion round failed: {}", exception.getMessage());
        }
    }

    Result ingestOnce() {
        List<SungrowTarget> found = targets.findTargets(properties.getBrand());
        if (found.isEmpty()) {
            return Result.EMPTY;
        }
        ZoneId zone = ZoneId.of(properties.getDeviceTimeZone());
        int saved = 0;
        int failed = 0;
        for (List<SungrowTarget> batch : partition(found, properties.getBatchSize())) {
            List<Map<String, Object>> devices;
            try {
                devices = fetch(batch);
                warnedNotAuthorized = false;
            } catch (SungrowApiException exception) {
                if (exception.isNotAuthorized()) {
                    if (!warnedNotAuthorized) {
                        log.warn("Sungrow is enabled but not authorized yet; "
                                + "send the authorization code to POST /api/v1/integrations/sungrow/authorization");
                        warnedNotAuthorized = true;
                    }
                    return Result.EMPTY;
                }
                if (exception.isRateLimited()) {
                    log.warn("Sungrow call limit reached ({}); stopping this round", exception.getCode());
                    break;
                }
                log.warn("Sungrow batch skipped: {}", exception.getMessage());
                continue;
            }
            Map<String, SungrowTarget> bySerial = new LinkedHashMap<>();
            batch.forEach(target -> bySerial.put(target.serialNumber(), target));
            for (Map<String, Object> device : devices) {
                int[] counts = saveDevice(device, bySerial, zone);
                saved += counts[0];
                failed += counts[1];
            }
        }
        return new Result(found.size(), saved, failed);
    }

    private List<Map<String, Object>> fetch(List<SungrowTarget> batch) {
        List<String> serials = batch.stream().map(SungrowTarget::serialNumber).toList();
        int stringCount = batch.stream().mapToInt(SungrowTarget::stringCount).max().orElse(0);
        return source.realTime(serials, SungrowPointMapper.pointIds(stringCount));
    }

    private int[] saveDevice(Map<String, Object> device, Map<String, SungrowTarget> bySerial, ZoneId zone) {
        String serial = SungrowPointMapper.serialOf(device);
        SungrowTarget target = serial == null ? null : bySerial.get(serial);
        if (target == null) {
            return new int[] {0, 0};
        }
        OffsetDateTime measuredAt = SungrowPointMapper.parseDeviceTime(SungrowPointMapper.deviceTimeOf(device), zone);
        if (measuredAt == null) {
            log.warn("Sungrow returned no valid device_time for inverter {}; readings skipped", serial);
            return new int[] {0, 0};
        }
        String faultCode = SungrowPointMapper.faultCode(device);
        List<SungrowPointMapper.StringReading> readings =
                SungrowPointMapper.readings(device, target.stringCount(), target.stringsPerMppt());
        int saved = 0;
        int failed = 0;
        for (SungrowPointMapper.StringReading reading : readings) {
            if (!target.activeSlots().contains(reading.slot())) {
                continue;
            }
            try {
                db.ingestStringMeasurement(serial, reading.slot().mppt(), reading.slot().entry(), measuredAt,
                        reading.powerW(), reading.voltageV(), reading.currentA(), null, faultCode,
                        properties.effectiveDataSource());
                saved++;
            } catch (DataAccessException exception) {
                failed++;
                log.warn("Reading of inverter {} (MPPT {}, entry {}) was rejected by the database: {}", serial,
                        reading.slot().mppt(), reading.slot().entry(), exception.getMostSpecificCause().getMessage());
            }
        }
        return new int[] {saved, failed};
    }

    private static <T> List<List<T>> partition(List<T> list, int size) {
        List<List<T>> parts = new ArrayList<>();
        for (int from = 0; from < list.size(); from += size) {
            parts.add(list.subList(from, Math.min(list.size(), from + size)));
        }
        return parts;
    }
}
