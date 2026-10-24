package org.apollo.api.sungrow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apollo.api.util.DbProcedures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

class SungrowIngestionJobTest {

    private static final OffsetDateTime MEASURED_AT = OffsetDateTime.parse("2026-10-23T12:00:00-03:00");

    private SungrowProperties properties;
    private SungrowRealTimeSource source;
    private SungrowTargetRepository targets;
    private DbProcedures db;
    private SungrowIngestionJob job;

    @BeforeEach
    void setUp() {
        properties = new SungrowProperties();
        source = mock(SungrowRealTimeSource.class);
        targets = mock(SungrowTargetRepository.class);
        db = mock(DbProcedures.class);
        job = new SungrowIngestionJob(properties, source, targets, db);
    }

    // 2 MPPTs x 2 entradas = 4 strings; so (1,1), (1,2) e (2,1) estao ativas no Apollo.
    private static SungrowTarget target(String serial) {
        return new SungrowTarget(serial, 2, 2, Set.of(new SungrowPointMapper.StringSlot(1, 1),
                new SungrowPointMapper.StringSlot(1, 2), new SungrowPointMapper.StringSlot(2, 1)));
    }

    private static Map<String, Object> device(String serial, String faultStatus) {
        return Map.ofEntries(
                Map.entry("device_sn", serial), Map.entry("device_time", "20261023120000"),
                Map.entry("dev_fault_status", faultStatus),
                Map.entry("p96", "600"), Map.entry("p70", "8"),
                Map.entry("p97", "610"), Map.entry("p71", "7"),
                Map.entry("p98", "590"), Map.entry("p72", "6"),
                Map.entry("p99", "580"), Map.entry("p73", "5"));
    }

    @Test
    void shouldSaveOnlyTheStringsThatAreActiveInApollo() {
        when(targets.findTargets("Sungrow")).thenReturn(List.of(target("SN1")));
        when(source.realTime(eq(List.of("SN1")), anyList())).thenReturn(List.of(device("SN1", "4")));

        SungrowIngestionJob.Result result = job.ingestOnce();

        assertEquals(new SungrowIngestionJob.Result(1, 3, 0), result);
        verify(db).ingestStringMeasurement(eq("SN1"), eq(1), eq(1), eq(MEASURED_AT), eq(new BigDecimal("4800.00")),
                eq(new BigDecimal("600.00")), eq(new BigDecimal("8.00")), isNull(), isNull(), eq("SENSOR"));
        verify(db).ingestStringMeasurement(eq("SN1"), eq(1), eq(2), eq(MEASURED_AT), eq(new BigDecimal("4270.00")),
                any(), any(), isNull(), isNull(), eq("SENSOR"));
        verify(db).ingestStringMeasurement(eq("SN1"), eq(2), eq(1), eq(MEASURED_AT), eq(new BigDecimal("3540.00")),
                any(), any(), isNull(), isNull(), eq("SENSOR"));
        verify(db, never()).ingestStringMeasurement(anyString(), eq(2), eq(2), any(), any(), any(), any(), any(),
                any(), anyString());
    }

    @Test
    void shouldTagSimulatedPlantReadingsAsSimulator() {
        properties.setPlantMode("simulated");
        when(targets.findTargets("Sungrow")).thenReturn(List.of(target("SN1")));
        when(source.realTime(anyList(), anyList())).thenReturn(List.of(device("SN1", "4")));

        job.ingestOnce();

        verify(db, times(3)).ingestStringMeasurement(eq("SN1"), anyInt(), anyInt(), eq(MEASURED_AT), any(), any(),
                any(), isNull(), isNull(), eq("SIMULATOR"));
    }

    @Test
    void shouldSendTheFaultCodeWhenTheDeviceReportsAFault() {
        when(targets.findTargets("Sungrow")).thenReturn(List.of(target("SN1")));
        when(source.realTime(anyList(), anyList())).thenReturn(List.of(device("SN1", "1")));

        job.ingestOnce();

        verify(db, times(3)).ingestStringMeasurement(eq("SN1"), anyInt(), anyInt(), eq(MEASURED_AT), any(), any(),
                any(), isNull(), eq(SungrowPointMapper.FAULT_CODE), eq("SENSOR"));
    }

    @Test
    void shouldDoNothingBeforeTheFirstAuthorization() {
        when(targets.findTargets("Sungrow")).thenReturn(List.of(target("SN1")));
        when(source.realTime(anyList(), anyList()))
                .thenThrow(new SungrowApiException(SungrowApiException.NOT_AUTHORIZED, "no authorization"));

        assertEquals(new SungrowIngestionJob.Result(0, 0, 0), job.ingestOnce());

        verifyNoInteractions(db);
    }

    @Test
    void shouldNotCallSungrowWithoutInverters() {
        when(targets.findTargets("Sungrow")).thenReturn(List.of());

        job.ingestOnce();

        verifyNoInteractions(source, db);
    }

    @Test
    void shouldStopTheRoundWhenTheCallLimitIsReached() {
        properties.setBatchSize(1);
        when(targets.findTargets("Sungrow")).thenReturn(List.of(target("SN1"), target("SN2")));
        when(source.realTime(anyList(), anyList())).thenThrow(new SungrowApiException("E999", "limit"));

        SungrowIngestionJob.Result result = job.ingestOnce();

        assertEquals(0, result.readingsSaved());
        verify(source, times(1)).realTime(anyList(), anyList());
        verifyNoInteractions(db);
    }

    @Test
    void shouldCountReadingsRejectedByTheDatabaseAndKeepGoing() {
        when(targets.findTargets("Sungrow")).thenReturn(List.of(target("SN1")));
        when(source.realTime(anyList(), anyList())).thenReturn(List.of(device("SN1", "4")));
        doThrow(new DataIntegrityViolationException("no active string")).when(db).ingestStringMeasurement(
                eq("SN1"), eq(1), eq(2), any(), any(), any(), any(), any(), any(), anyString());

        SungrowIngestionJob.Result result = job.ingestOnce();

        assertEquals(new SungrowIngestionJob.Result(1, 2, 1), result);
    }

    @Test
    void shouldIgnoreDevicesThatWereNotRequestedOrHaveNoTime() {
        when(targets.findTargets("Sungrow")).thenReturn(List.of(target("SN1")));
        Map<String, Object> noTime = Map.of("device_sn", "SN1", "p96", "600", "p70", "8");
        when(source.realTime(anyList(), anyList()))
                .thenReturn(List.of(device("OTHER", "4"), noTime));

        assertEquals(0, job.ingestOnce().readingsSaved());
        verifyNoInteractions(db);
    }
}
