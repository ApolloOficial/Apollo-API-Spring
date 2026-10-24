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

/**
 * Traduz os pontos de medicao do inversor da Sungrow ("Common Inverter Measuring Points") em leituras
 * por string do Apollo. Classe sem Spring: so regras de mapeamento.
 * <p>
 * Pontos de corrente/tensao por string (strings 1 a 40):
 * <pre>
 *   string  1-16 : tensao 96..111      corrente 70..85
 *   string 17    : tensao 112          corrente 92
 *   string 18    : tensao 113          corrente 93
 *   string 19-32 : tensao 7166..7179   corrente 313..326
 *   string 33-40 : tensao 7707,7709..  corrente 7708,7710.. (de 2 em 2)
 * </pre>
 * A Sungrow nao tem potencia nem energia por string: a potencia e calculada (V x I) e a energia fica nula.
 */
public final class SungrowPointMapper {

    public static final int MAX_STRINGS = 40;

    /** Valor de dev_fault_status que indica falha do equipamento (2 = alarme, 4 = normal). */
    private static final int FAULT_STATUS = 1;
    /** Codigo gravado em string_performance_measurement.fault_code (o banco abre um alerta FALHA_INVERSOR). */
    public static final String FAULT_CODE = "SUNGROW_FAULT";

    private static final DateTimeFormatter DEVICE_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private SungrowPointMapper() {
    }

    /** Posicao da string no inversor do Apollo (colunas string.mppt_number e string.entry_number). */
    public record StringSlot(int mppt, int entry) {
    }

    /** Leitura de uma string. A energia nao existe na API da Sungrow. */
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

    /**
     * Hipotese a validar com um inversor real: a Sungrow numera as strings de 1 a N seguidas e o Apollo
     * guarda MPPT + entrada. Com stringsPerMppt = 2, a string 3 e MPPT 2 / entrada 1.
     */
    public static StringSlot slotOf(int stringNumber, int stringsPerMppt) {
        checkString(stringNumber);
        return new StringSlot((stringNumber - 1) / stringsPerMppt + 1, (stringNumber - 1) % stringsPerMppt + 1);
    }

    /** Quantas strings o modelo tem (limitado ao que a tabela de pontos cobre). */
    public static int stringCount(int mpptCount, int stringsPerMppt) {
        return Math.max(0, Math.min(MAX_STRINGS, mpptCount * stringsPerMppt));
    }

    /** Ids dos pontos (tensao e corrente) a pedir para as strings 1..stringCount. */
    public static List<String> pointIds(int stringCount) {
        List<String> ids = new ArrayList<>();
        for (int n = 1; n <= Math.min(stringCount, MAX_STRINGS); n++) {
            ids.add(String.valueOf(voltagePoint(n)));
            ids.add(String.valueOf(currentPoint(n)));
        }
        return ids;
    }

    /**
     * Le as leituras por string de um dispositivo da resposta em tempo real. Strings sem tensao ou sem
     * corrente (ponto nao devolvido pelo modelo) sao ignoradas: o Apollo nao aceita potencia nula.
     */
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

    /** device_time (yyyyMMddHHmmss) no fuso informado; nulo se vier vazio ou fora do formato. */
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

    /** FAULT_CODE quando dev_fault_status indica falha; nulo para normal, alarme ou ausente. */
    public static String faultCode(Map<String, Object> device) {
        BigDecimal status = number(pointsOf(device).get("dev_fault_status"));
        return status != null && status.intValue() == FAULT_STATUS ? FAULT_CODE : null;
    }

    /** Numero de serie do dispositivo, ou nulo. */
    public static String serialOf(Map<String, Object> device) {
        Object serial = pointsOf(device).get("device_sn");
        return serial == null || String.valueOf(serial).isBlank() ? null : String.valueOf(serial).trim();
    }

    /** device_time cru do dispositivo, ou nulo. */
    public static Object deviceTimeOf(Map<String, Object> device) {
        return pointsOf(device).get("device_time");
    }

    /** Aceita o item "plano" ou aninhado em "device_point" (as duas formas aparecem na API). */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> pointsOf(Map<String, Object> device) {
        Object nested = device.get("device_point");
        return nested instanceof Map<?, ?> map ? (Map<String, Object>) map : device;
    }

    /** Converte numero ou texto numerico; vazio, "--", NaN e texto qualquer viram nulo. */
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
