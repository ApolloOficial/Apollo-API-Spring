package org.apollo.api.sungrow;

import java.util.Set;

public class SungrowApiException extends RuntimeException {
    private static final Set<String> INVALID_TOKEN = Set.of("invalid_token", "E003");
    private static final Set<String> RATE_LIMITED = Set.of("E901", "E998", "E999");

    public static final String NOT_AUTHORIZED = "NOT_AUTHORIZED";

    private final String code;

    public SungrowApiException(String code, String message) {
        super("Sungrow error " + code + (message == null || message.isBlank() ? "" : ": " + message));
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public boolean isNotAuthorized() {
        return NOT_AUTHORIZED.equals(code);
    }

    public boolean isInvalidToken() {
        return INVALID_TOKEN.contains(code);
    }

    public boolean isRateLimited() {
        return RATE_LIMITED.contains(code);
    }
}
