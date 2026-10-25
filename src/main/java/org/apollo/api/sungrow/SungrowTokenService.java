package org.apollo.api.sungrow;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnSungrowLive
public class SungrowTokenService {
    private static final Logger log = LoggerFactory.getLogger(SungrowTokenService.class);
    private static final long DEFAULT_EXPIRES_IN_SECONDS = 3600;

    private final SungrowClient client;
    private final SungrowTokenStore store;
    private final SungrowProperties properties;
    private final Clock clock;

    @Autowired
    public SungrowTokenService(SungrowClient client, SungrowTokenStore store, SungrowProperties properties) {
        this(client, store, properties, Clock.systemUTC());
    }

    SungrowTokenService(SungrowClient client, SungrowTokenStore store, SungrowProperties properties, Clock clock) {
        this.client = client;
        this.store = store;
        this.properties = properties;
        this.clock = clock;
    }

    public SungrowTokens authorize(String code) {
        SungrowTokenResponse.TokenData data = client.exchangeCode(code);
        SungrowTokens tokens = toTokens(data, null);
        store.save(tokens);
        log.info("Sungrow authorization stored; access token valid until {}", tokens.expiresAt());
        return tokens;
    }

    public Optional<SungrowTokens> current() {
        return store.find();
    }

    public Optional<String> accessToken() {
        Optional<SungrowTokens> stored = store.find();
        if (stored.isEmpty()) {
            return Optional.empty();
        }
        if (!expiresSoon(stored.get())) {
            return Optional.of(stored.get().accessToken());
        }
        return Optional.of(refresh(stored.get().accessToken()));
    }

    public String refresh(String staleAccessToken) {
        return store.withLock(() -> {
            SungrowTokens stored = store.find()
                    .orElseThrow(() -> new SungrowApiException(SungrowApiException.NOT_AUTHORIZED, "no stored Sungrow authorization"));
            if (!stored.accessToken().equals(staleAccessToken) && !expiresSoon(stored)) {
                return stored.accessToken();
            }
            SungrowTokens renewed = toTokens(client.refresh(stored.refreshToken()), stored);
            store.save(renewed);
            log.info("Sungrow token renewed; valid until {}", renewed.expiresAt());
            return renewed.accessToken();
        });
    }

    private boolean expiresSoon(SungrowTokens tokens) {
        Instant limit = clock.instant().plusSeconds(properties.getTokenRefreshMarginSeconds());
        return !tokens.expiresAt().isAfter(limit);
    }

    private SungrowTokens toTokens(SungrowTokenResponse.TokenData data, SungrowTokens previous) {
        long expiresIn = data.expiresIn() != null && data.expiresIn() > 0 ? data.expiresIn() : DEFAULT_EXPIRES_IN_SECONDS;
        String plants = data.authPsList() == null || data.authPsList().isEmpty()
                ? (previous == null ? null : previous.authorizedPlants())
                : data.authPsList().stream().map(String::valueOf).collect(Collectors.joining(","));
        return new SungrowTokens(data.accessToken(), data.refreshToken(), clock.instant().plusSeconds(expiresIn), plants);
    }
}
