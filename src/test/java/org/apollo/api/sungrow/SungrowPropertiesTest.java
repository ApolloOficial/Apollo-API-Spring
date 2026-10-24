package org.apollo.api.sungrow;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class SungrowPropertiesTest {

    private static SungrowProperties enabledWithCredentials() {
        SungrowProperties properties = new SungrowProperties();
        properties.setEnabled(true);
        properties.setAppkey("app");
        properties.setAccessKey("key");
        properties.setRedirectUri("https://apollo.test/callback");
        return properties;
    }

    @Test
    void shouldAcceptADisabledIntegrationWithoutCredentials() {
        assertDoesNotThrow(() -> new SungrowProperties().afterPropertiesSet());
    }

    @Test
    void shouldRequireCredentialsWhenEnabled() {
        SungrowProperties properties = enabledWithCredentials();
        properties.setAppkey(" ");

        assertThrows(IllegalStateException.class, properties::afterPropertiesSet);
    }

    @Test
    void shouldRejectARealSensorIntervalBelowFiveMinutes() {
        SungrowProperties properties = enabledWithCredentials();
        properties.setPollIntervalSeconds(60);

        assertThrows(IllegalStateException.class, properties::afterPropertiesSet);
    }

    @Test
    void shouldNeedNoCredentialsAndAllowShortIntervalsWithASimulatedPlant() {
        SungrowProperties properties = new SungrowProperties();
        properties.setEnabled(true);
        properties.setPlantMode("Simulated");
        properties.setPollIntervalSeconds(30);

        assertDoesNotThrow(properties::afterPropertiesSet);
        assertEquals("SIMULATOR", properties.effectiveDataSource());
    }

    @Test
    void shouldNeverTagASimulatedPlantAsSensor() {
        SungrowProperties properties = new SungrowProperties();
        properties.setPlantMode("simulated");
        properties.setDataSource("SENSOR");

        assertEquals("SIMULATOR", properties.effectiveDataSource());
    }

    @Test
    void shouldRejectAnUnknownPlantMode() {
        SungrowProperties properties = enabledWithCredentials();
        properties.setPlantMode("fake");

        assertThrows(IllegalStateException.class, properties::afterPropertiesSet);
    }

    @Test
    void shouldRejectAnUnknownDataSource() {
        SungrowProperties properties = enabledWithCredentials();
        properties.setDataSource("REAL");

        assertThrows(IllegalStateException.class, properties::afterPropertiesSet);
    }
}
