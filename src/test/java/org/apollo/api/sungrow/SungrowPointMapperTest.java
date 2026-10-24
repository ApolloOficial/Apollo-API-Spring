package org.apollo.api.sungrow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SungrowPointMapperTest {
    private static final ZoneId SAO_PAULO = ZoneId.of("America/Sao_Paulo");

    @Test
    void shouldMapVoltageAndCurrentPointsOfEachStringRange() {
        assertEquals(96, SungrowPointMapper.voltagePoint(1));
        assertEquals(70, SungrowPointMapper.currentPoint(1));
        assertEquals(111, SungrowPointMapper.voltagePoint(16));
        assertEquals(85, SungrowPointMapper.currentPoint(16));
        assertEquals(112, SungrowPointMapper.voltagePoint(17));
        assertEquals(92, SungrowPointMapper.currentPoint(17));
        assertEquals(113, SungrowPointMapper.voltagePoint(18));
        assertEquals(93, SungrowPointMapper.currentPoint(18));
        assertEquals(7166, SungrowPointMapper.voltagePoint(19));
        assertEquals(313, SungrowPointMapper.currentPoint(19));
        assertEquals(7171, SungrowPointMapper.voltagePoint(24));
        assertEquals(318, SungrowPointMapper.currentPoint(24));
        assertEquals(7179, SungrowPointMapper.voltagePoint(32));
        assertEquals(326, SungrowPointMapper.currentPoint(32));
        assertEquals(7707, SungrowPointMapper.voltagePoint(33));
        assertEquals(7708, SungrowPointMapper.currentPoint(33));
        assertEquals(7721, SungrowPointMapper.voltagePoint(40));
        assertEquals(7722, SungrowPointMapper.currentPoint(40));
    }

    @Test
    void shouldRejectStringNumbersOutsideTheTable() {
        assertThrows(IllegalArgumentException.class, () -> SungrowPointMapper.voltagePoint(0));
        assertThrows(IllegalArgumentException.class, () -> SungrowPointMapper.currentPoint(41));
    }

    @Test
    void shouldListPointIdsForTheRequestedStrings() {
        List<String> ids = SungrowPointMapper.pointIds(24);

        assertEquals(48, ids.size());
        assertTrue(ids.contains("96") && ids.contains("70"));
        assertTrue(ids.contains("7171") && ids.contains("318"));
        assertEquals(80, SungrowPointMapper.pointIds(99).size());
    }

    @Test
    void shouldConvertTheStringNumberIntoMpptAndEntry() {
        assertEquals(new SungrowPointMapper.StringSlot(1, 1), SungrowPointMapper.slotOf(1, 2));
        assertEquals(new SungrowPointMapper.StringSlot(1, 2), SungrowPointMapper.slotOf(2, 2));
        assertEquals(new SungrowPointMapper.StringSlot(2, 1), SungrowPointMapper.slotOf(3, 2));
        assertEquals(new SungrowPointMapper.StringSlot(2, 2), SungrowPointMapper.slotOf(5, 3));
        assertEquals(8, SungrowPointMapper.stringCount(4, 2));
        assertEquals(40, SungrowPointMapper.stringCount(20, 3));
    }

    @Test
    void shouldBuildReadingsWithPowerAsVoltageTimesCurrent() {
        Map<String, Object> device = Map.of("p96", "600.5", "p70", "8.2", "p97", 610, "p71", 7);

        List<SungrowPointMapper.StringReading> readings = SungrowPointMapper.readings(device, 2, 2);

        assertEquals(2, readings.size());
        SungrowPointMapper.StringReading first = readings.get(0);
        assertEquals(new SungrowPointMapper.StringSlot(1, 1), first.slot());
        assertEquals(new BigDecimal("600.50"), first.voltageV());
        assertEquals(new BigDecimal("8.20"), first.currentA());
        assertEquals(new BigDecimal("4924.10"), first.powerW());
        assertEquals(new BigDecimal("4270.00"), readings.get(1).powerW());
    }

    @Test
    void shouldSkipStringsWithoutVoltageOrCurrent() {
        Map<String, Object> device = Map.of("p96", "600", "p70", "--", "p97", "610");

        assertTrue(SungrowPointMapper.readings(device, 2, 2).isEmpty());
    }

    @Test
    void shouldReadPointsNestedInDevicePoint() {
        Map<String, Object> device = Map.of("device_point", Map.of("device_sn", "SN1", "p96", "500", "p70", "4"));

        assertEquals("SN1", SungrowPointMapper.serialOf(device));
        assertEquals(1, SungrowPointMapper.readings(device, 1, 2).size());
    }

    @Test
    void shouldParseTheDeviceTimeInTheConfiguredZone() {
        OffsetDateTime parsed = SungrowPointMapper.parseDeviceTime("20261023120500", SAO_PAULO);

        assertEquals(OffsetDateTime.parse("2026-10-23T12:05:00-03:00"), parsed);
        assertNull(SungrowPointMapper.parseDeviceTime("not a date", SAO_PAULO));
        assertNull(SungrowPointMapper.parseDeviceTime(null, SAO_PAULO));
    }

    @Test
    void shouldOnlyFlagAFaultWhenTheDeviceStatusSaysFault() {
        assertEquals(SungrowPointMapper.FAULT_CODE, SungrowPointMapper.faultCode(Map.of("dev_fault_status", "1")));
        assertEquals(SungrowPointMapper.FAULT_CODE, SungrowPointMapper.faultCode(Map.of("dev_fault_status", 1)));
        assertNull(SungrowPointMapper.faultCode(Map.of("dev_fault_status", "2")));
        assertNull(SungrowPointMapper.faultCode(Map.of("dev_fault_status", "4")));
        assertNull(SungrowPointMapper.faultCode(Map.of()));
    }

    @Test
    void shouldTurnUnusableValuesIntoNull() {
        assertNull(SungrowPointMapper.number(null));
        assertNull(SungrowPointMapper.number(""));
        assertNull(SungrowPointMapper.number("abc"));
        assertEquals(new BigDecimal("12.5"), SungrowPointMapper.number(" 12.5 "));
    }
}
