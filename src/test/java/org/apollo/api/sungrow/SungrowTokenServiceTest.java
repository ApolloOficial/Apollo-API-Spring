package org.apollo.api.sungrow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class SungrowTokenServiceTest {
    private static final Instant NOW = Instant.parse("2026-10-23T12:00:00Z");

    private SungrowClient client;
    private SungrowTokenStore store;
    private SungrowTokenService service;

    @BeforeEach
    void setUp() {
        client = mock(SungrowClient.class);
        store = mock(SungrowTokenStore.class);
        SungrowProperties properties = new SungrowProperties();
        properties.setTokenRefreshMarginSeconds(600);
        service = new SungrowTokenService(client, store, properties, Clock.fixed(NOW, ZoneOffset.UTC));
        when(store.withLock(any())).thenAnswer(invocation -> ((Supplier<?>) invocation.getArgument(0)).get());
    }

    @Test
    void shouldStoreTheTokensReturnedForTheAuthorizationCode() {
        when(client.exchangeCode("abc")).thenReturn(
                new SungrowTokenResponse.TokenData("AT", "RT", 172799L, List.of(111, 222), null));

        SungrowTokens tokens = service.authorize("abc");

        assertEquals(NOW.plusSeconds(172799), tokens.expiresAt());
        assertEquals("111,222", tokens.authorizedPlants());
        ArgumentCaptor<SungrowTokens> saved = ArgumentCaptor.forClass(SungrowTokens.class);
        verify(store).save(saved.capture());
        assertEquals("AT", saved.getValue().accessToken());
        assertEquals("RT", saved.getValue().refreshToken());
    }

    @Test
    void shouldReturnNothingBeforeTheFirstAuthorization() {
        when(store.find()).thenReturn(Optional.empty());

        assertTrue(service.accessToken().isEmpty());
        verify(client, never()).refresh(any());
    }

    @Test
    void shouldReuseATokenThatIsFarFromExpiring() {
        when(store.find()).thenReturn(Optional.of(new SungrowTokens("AT", "RT", NOW.plusSeconds(3600), "1")));

        assertEquals("AT", service.accessToken().orElseThrow());
        verify(client, never()).refresh(any());
    }

    @Test
    void shouldRenewATokenThatIsAboutToExpire() {
        when(store.find()).thenReturn(Optional.of(new SungrowTokens("OLD", "RT", NOW.plusSeconds(60), "111")));
        when(client.refresh("RT")).thenReturn(new SungrowTokenResponse.TokenData("NEW", "RT2", 1000L, null, 1));

        assertEquals("NEW", service.accessToken().orElseThrow());

        ArgumentCaptor<SungrowTokens> saved = ArgumentCaptor.forClass(SungrowTokens.class);
        verify(store).save(saved.capture());
        assertEquals("RT2", saved.getValue().refreshToken());
        assertEquals(NOW.plusSeconds(1000), saved.getValue().expiresAt());
        assertEquals("111", saved.getValue().authorizedPlants());
    }

    @Test
    void shouldNotSpendTheRefreshTokenWhenAnotherInstanceAlreadyRenewed() {
        when(store.find()).thenReturn(Optional.of(new SungrowTokens("FRESH", "RT", NOW.plusSeconds(3600), "1")));

        assertEquals("FRESH", service.refresh("STALE"));
        verify(client, never()).refresh(any());
    }

    @Test
    void shouldRenewWhenTheCurrentTokenWasRejectedEvenIfItLooksValid() {
        when(store.find()).thenReturn(Optional.of(new SungrowTokens("REJECTED", "RT", NOW.plusSeconds(3600), "1")));
        when(client.refresh("RT")).thenReturn(new SungrowTokenResponse.TokenData("NEW", "RT2", 1000L, null, 1));

        assertEquals("NEW", service.refresh("REJECTED"));
    }
}
