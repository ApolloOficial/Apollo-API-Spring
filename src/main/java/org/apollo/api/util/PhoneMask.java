package org.apollo.api.util;

public final class PhoneMask {

    private PhoneMask() {
    }

    public static String digitsOnly(String raw) {
        return raw == null ? "" : raw.replaceAll("\\D", "");
    }

    public static boolean isValid(String digits) {
        if (digits == null || !digits.matches("\\d{10,11}")) {
            return false;
        }
        if (digits.charAt(0) == '0' || digits.charAt(1) == '0') {
            return false;
        }
        return digits.length() == 10 || digits.charAt(2) == '9';
    }

    public static String mask(String digits) {
        if (digits == null || !digits.matches("\\d{10,11}")) {
            return null;
        }
        String area = digits.substring(0, 2);
        String last = digits.substring(digits.length() - 4);
        if (digits.length() == 11) {
            return "(" + area + ") " + digits.charAt(2) + "****-" + last;
        }
        return "(" + area + ") ****-" + last;
    }
}
