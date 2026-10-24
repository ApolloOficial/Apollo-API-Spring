package org.apollo.api.sungrow;

import java.util.Set;

/**
 * Falha devolvida pela Sungrow (result_code diferente de "1", ou {"error":"invalid_token"}) ou
 * falha de rede/resposta ilegivel. O texto nunca carrega tokens ou chaves.
 */
public class SungrowApiException extends RuntimeException {

    // Appendix 2 da API: E003 token invalido/expirado.
    private static final Set<String> INVALID_TOKEN = Set.of("invalid_token", "E003");
    // E901 frequencia alta, E998 limite mensal, E999 limite por hora.
    private static final Set<String> RATE_LIMITED = Set.of("E901", "E998", "E999");

    /** Ainda nao ha autorizacao (tokens) guardada. */
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
