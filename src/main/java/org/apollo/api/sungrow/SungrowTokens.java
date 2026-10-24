package org.apollo.api.sungrow;

import java.time.Instant;

/** Tokens da Sungrow guardados no banco (tabela sungrow_credential, uma unica linha). */
public record SungrowTokens(String accessToken, String refreshToken, Instant expiresAt, String authorizedPlants) {

    @Override
    public String toString() {
        // Nunca expor os tokens em log.
        return "SungrowTokens[expiresAt=" + expiresAt + ", authorizedPlants=" + authorizedPlants + "]";
    }
}
