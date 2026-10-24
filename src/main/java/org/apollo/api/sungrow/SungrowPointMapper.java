package org.apollo.api.sungrow;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class SungrowPointMapper {
    public static final int MAX_STRINGS = 40;

    private static final int FAULT_STATUS = 1;
    public static final String FAULT_CODE = "SUNGROW_FAULT";

    private static final DateTimeFormatter DEVICE_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private SungrowPointMapper() {
    }

    public record StringSlot(int mppt, int entry) {
    }

    public record StringReading(int stringNumber, StringSlot slot, BigDecimal voltageV, BigDecimal currentA,
                                BigDecimal powerW) {
    }

    public static int voltagePoint(int stringNumber) {
        checkString(stringNumber);
        if (stringNumber <= 16) {
            return 96 + (stringNumber - 1);
        }
        if (stringNumber == 17) {
            return 112;
        }
        if (stringNumber == 18) {
            return 113;
        }
        if (stringNumber <= 32) {
            return 7166 + (stringNumber - 19);
        }
        return 7707 + 2 * (stringNumber - 33);
    }

    public static int currentPoint(int stringNumber) {
        checkString(stringNumber);
        if (stringNumber <= 16) {
            return 70 + (stringNumber - 1);
        }
        if (stringNumber == 17) {
            return 92;
        }
        if (stringNumber == 18) {
            return 93;
        }
        if (stringNumber <= 32) {
            return 313 + (stringNumber - 19);
        }
        return 7708 + 2 * (stringNumber - 33);
    }

    public static StringSlot slotOf(int stringNumber, int stringsPerMppt) {
        checkString(stringNumber);
        return new StringSlot((stringNumber - 1) / stringsPerMppt + 1, (stringNumber - 1) % stringsPerMppt + 1);
    }

    public static int stringCount(int mpptCount, int stringsPerMppt) {
        return Math.max(0, Math.min(MAX_STRINGS, mpptCount * stringsPerMppt));
    }

    public static List<String> pointIds(int stringCount) {
        List<String> ids = new ArrayList<>();
        for (int n = 1; n <= Math.min(stringCount, MAX_STRINGS); n++) {
            ids.add(String.valueOf(voltagePoint(n)));
            ids.add(String.valueOf(currentPoint(n)));
        }
        return ids;
    }

    public static List<StringReading> readings(Map<String, Object> device, int stringCount, int stringsPerMppt) {
        Map<String, Object> points = pointsOf(device);
        List<StringReading> readings = new ArrayList<>();
        for (int n = 1; n <= Math.min(stringCount, MAX_STRINGS); n++) {
            BigDecimal voltage = number(points.get("p" + voltagePoint(n)));
            BigDecimal current = number(points.get("p" + currentPoint(n)));
            if (voltage == null || current == null) {
                continue;
            }
            BigDecimal power = voltage.multiply(current).setScale(2, RoundingMode.HALF_UP);
            readings.add(new StringReading(n, slotOf(n, stringsPerMppt), voltage.setScale(2, RoundingMode.HALF_UP),
                    current.setScale(2, RoundingMode.HALF_UP), power));
        }
        return readings;
    }

    public static OffsetDateTime parseDeviceTime(Object value, ZoneId zone) {
        if (value == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(String.valueOf(value).trim(), DEVICE_TIME).atZone(zone).toOffsetDateTime();
        } catch (DateTimeParseException exception) {
            return null;
        }
    }

    public static String faultCode(Map<String, Object> device) {
        BigDecimal status = number(pointsOf(device).get("dev_fault_status"));
        return status != null && status.intValue() == FAULT_STATUS ? FAULT_CODE : null;
    }

    public static String serialOf(Map<String, Object> device) {
        Object serial = pointsOf(device).get("device_sn");
        return serial == null || String.valueOf(serial).isBlank() ? null : String.valueOf(serial).trim();
    }

    public static Object deviceTimeOf(Map<String, Object> device) {
        return pointsOf(device).get("device_time");
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> pointsOf(Map<String, Object> device) {
        Object nested = device.get("device_point");
        return nested instanceof Map<?, ?> map ? (Map<String, Object>) map : device;
    }

    static BigDecimal number(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        if (text.isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(text);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private static void checkString(int stringNumber) {
        if (stringNumber < 1 || stringNumber > MAX_STRINGS) {
            throw new IllegalArgumentException("String number must be between 1 and " + MAX_STRINGS);
        }
    }
}
