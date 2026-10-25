package org.apollo.api.sungrow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SungrowSimulatedPlantTest {
    private static final ZoneId SAO_PAULO = ZoneId.of("America/Sao_Paulo");
    private static final List<String> POINTS = SungrowPointMapper.pointIds(4);

    private static Clock at(String instant) {
        return Clock.fixed(Instant.parse(instant), SAO_PAULO);
    }

    private static SungrowSimulatedPlant plant(SungrowProperties properties, String instant) {
        properties.setPlantMode("simulated");
        return new SungrowSimulatedPlant(properties, at(instant));
    }

    private static final String NOON = "2026-10-23T15:07:00Z";
    private static final String NIGHT = "2026-10-23T04:00:00Z";

    @Test
    void shouldReturnADeviceInTheFormatTheSungrowApiUses() {
        Map<String, Object> device = plant(new SungrowProperties(), NOON).realTime(List.of("SN1"), POINTS).get(0);

        assertEquals("SN1", device.get("device_sn"));
        assertEquals("20261023120500", device.get("device_time"));
        assertEquals("4", device.get("dev_fault_status"));
        assertTrue(device.containsKey("p96") && device.containsKey("p70"));
    }

    @Test
    void shouldFeedTheSameMapperTheRealApiFeeds() {
        Map<String, Object> device = plant(new SungrowProperties(), NOON).realTime(List.of("SN1"), POINTS).get(0);

        List<SungrowPointMapper.StringReading> readings = SungrowPointMapper.readings(device, 4, 2);

        assertEquals(4, readings.size());
        for (SungrowPointMapper.StringReading reading : readings) {
            assertTrue(reading.voltageV().compareTo(new BigDecimal("450")) > 0);
            assertTrue(reading.currentA().compareTo(BigDecimal.ZERO) > 0);
            assertTrue(reading.powerW().compareTo(BigDecimal.ZERO) > 0);
        }
    }

    @Test
    void shouldProduceNothingAtNight() {
        Map<String, Object> device = plant(new SungrowProperties(), NIGHT).realTime(List.of("SN1"), POINTS).get(0);

        assertEquals("0.0", device.get("p96"));
        assertEquals("0.0", device.get("p70"));
        assertEquals("0", device.get("dev_status"));
    }

    @Test
    void shouldKeepSunAllDayWhenAskedTo() {
        SungrowProperties properties = new SungrowProperties();
        properties.setSimulationAlwaysSun(true);

        Map<String, Object> device = plant(properties, NIGHT).realTime(List.of("SN1"), POINTS).get(0);

        assertFalse("0.0".equals(device.get("p70")));
    }

    @Test
    void shouldReportAFaultOnlyForTheConfiguredSerials() {
        SungrowProperties properties = new SungrowProperties();
        properties.setSimulationFaultSerials("SNFAIL");

        List<Map<String, Object>> devices = plant(properties, NOON).realTime(List.of("SN1", "SNFAIL"), POINTS);

        assertEquals("4", devices.get(0).get("dev_fault_status"));
        assertEquals("1", devices.get(1).get("dev_fault_status"));
        assertEquals(SungrowPointMapper.FAULT_CODE, SungrowPointMapper.faultCode(devices.get(1)));
    }

    @Test
    void shouldWeakenOnlyTheConfiguredString() {
        SungrowProperties properties = new SungrowProperties();
        properties.setSimulationWeakStrings("SN1:2:0.5");

        Map<String, Object> device = plant(properties, NOON).realTime(List.of("SN1"), POINTS).get(0);
        double healthy = Double.parseDouble((String) device.get("p70"));
        double weak = Double.parseDouble((String) device.get("p71"));

        assertTrue(weak < healthy * 0.65, "weak string should produce about half of a healthy one");
    }

    @Test
    void shouldIgnoreUnknownPointsAndRejectMalformedWeakStrings() {
        Map<String, Object> device = plant(new SungrowProperties(), NOON)
                .realTime(List.of("SN1"), List.of("96", "999999", "abc")).get(0);

        assertTrue(device.containsKey("p96"));
        assertFalse(device.containsKey("p999999"));

        SungrowProperties bad = new SungrowProperties();
        bad.setSimulationWeakStrings("SN1:2");
        assertThrows(IllegalStateException.class, () -> new SungrowSimulatedPlant(bad, at(NOON)));
    }
}
