package org.apollo.api.recovery;

public final class EmailMask {

    private EmailMask() {
    }

    public static String mask(String email) {
        if (email == null) {
            return "";
        }
        String trimmed = email.trim();
        int at = trimmed.indexOf('@');
        if (at < 1 || at == trimmed.length() - 1) {
            return "***";
        }
        return trimmed.charAt(0) + "***" + trimmed.substring(at);
    }
}
