package org.apollo.api.sungrow;

import java.time.Instant;

public record SungrowTokens(String accessToken, String refreshToken, Instant expiresAt, String authorizedPlants) {
    @Override
    public String toString() {
        return "SungrowTokens[expiresAt=" + expiresAt + ", authorizedPlants=" + authorizedPlants + "]";
    }
}
