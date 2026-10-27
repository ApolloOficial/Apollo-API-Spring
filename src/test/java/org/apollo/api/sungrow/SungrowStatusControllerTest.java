package org.apollo.api.sungrow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

class SungrowStatusControllerTest {
    private static final Instant EXPIRES_AT = Instant.parse("2026-10-25T12:00:00Z");

    private SungrowProperties properties;
    private ObjectProvider<SungrowTokenService> tokens;
    private SungrowTokenService tokenService;
    private SungrowStatusController controller;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        properties = new SungrowProperties();
        tokens = mock(ObjectProvider.class);
        tokenService = mock(SungrowTokenService.class);
        controller = new SungrowStatusController(properties, tokens);
    }

    @Test
    void shouldReportDisabledWhenTheIntegrationIsOff() {
        properties.setEnabled(false);

        SungrowStatusController.Status status = controller.status();

        assertFalse(status.enabled());
        assertEquals("DISABLED", status.mode());
        assertFalse(status.authorized());
    }

    @Test
    void shouldReportSimulatedWithoutNeedingAuthorization() {
        properties.setEnabled(true);
        properties.setPlantMode("simulated");

        SungrowStatusController.Status status = controller.status();

        assertTrue(status.enabled());
        assertEquals("SIMULATED", status.mode());
        assertFalse(status.authorized());
        assertNull(status.accessTokenExpiresAt());
    }

    @Test
    void shouldReportLiveNotAuthorizedWhenThereAreNoStoredTokens() {
        properties.setEnabled(true);
        properties.setPlantMode("live");
        when(tokens.getIfAvailable()).thenReturn(tokenService);
        when(tokenService.current()).thenReturn(Optional.empty());

        SungrowStatusController.Status status = controller.status();

        assertEquals("LIVE", status.mode());
        assertFalse(status.authorized());
    }

    @Test
    void shouldReportLiveAuthorizedWithoutExposingTheTokens() {
        properties.setEnabled(true);
        properties.setPlantMode("live");
        when(tokens.getIfAvailable()).thenReturn(tokenService);
        when(tokenService.current()).thenReturn(Optional.of(new SungrowTokens("AT", "RT", EXPIRES_AT, "111,222")));

        SungrowStatusController.Status status = controller.status();

        assertEquals("LIVE", status.mode());
        assertTrue(status.authorized());
        assertEquals(EXPIRES_AT, status.accessTokenExpiresAt());
        assertEquals("111,222", status.authorizedPlants());
    }
}
