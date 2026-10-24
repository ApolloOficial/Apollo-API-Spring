package org.apollo.api.sungrow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SungrowLiveSourceTest {
    private static final List<String> SERIALS = List.of("SN1");
    private static final List<String> POINTS = List.of("96", "70");
    private static final List<Map<String, Object>> DEVICES = List.of(Map.of("device_sn", "SN1"));

    private SungrowTokenService tokens;
    private SungrowClient client;
    private SungrowLiveSource source;

    @BeforeEach
    void setUp() {
        tokens = mock(SungrowTokenService.class);
        client = mock(SungrowClient.class);
        source = new SungrowLiveSource(tokens, client);
    }

    @Test
    void shouldReadWithTheStoredToken() {
        when(tokens.accessToken()).thenReturn(Optional.of("AT"));
        when(client.realTime("AT", SERIALS, POINTS)).thenReturn(DEVICES);

        assertEquals(DEVICES, source.realTime(SERIALS, POINTS));
    }

    @Test
    void shouldFailAsNotAuthorizedBeforeTheFirstAuthorization() {
        when(tokens.accessToken()).thenReturn(Optional.empty());

        SungrowApiException exception = assertThrows(SungrowApiException.class, () -> source.realTime(SERIALS, POINTS));

        assertTrue(exception.isNotAuthorized());
        verify(client, never()).realTime(any(), any(), any());
    }

    @Test
    void shouldRenewTheTokenOnceAndRetryWhenSungrowRejectsIt() {
        when(tokens.accessToken()).thenReturn(Optional.of("AT"));
        when(client.realTime("AT", SERIALS, POINTS)).thenThrow(new SungrowApiException("invalid_token", "expired"));
        when(tokens.refresh("AT")).thenReturn("AT2");
        when(client.realTime("AT2", SERIALS, POINTS)).thenReturn(DEVICES);

        assertEquals(DEVICES, source.realTime(SERIALS, POINTS));
        verify(tokens).refresh("AT");
    }

    @Test
    void shouldNotRenewForOtherErrors() {
        when(tokens.accessToken()).thenReturn(Optional.of("AT"));
        when(client.realTime("AT", SERIALS, POINTS)).thenThrow(new SungrowApiException("E999", "limit"));

        SungrowApiException exception = assertThrows(SungrowApiException.class, () -> source.realTime(SERIALS, POINTS));

        assertTrue(exception.isRateLimited());
        verify(tokens, never()).refresh(any());
    }
}
