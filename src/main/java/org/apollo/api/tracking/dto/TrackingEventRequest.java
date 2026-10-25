package org.apollo.api.tracking.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record TrackingEventRequest(
        @JsonProperty("event_id") @NotNull UUID eventId,
        @JsonProperty("event_name") @NotBlank @Size(max = 64) String eventName,
        @JsonProperty("session_id") @NotNull UUID sessionId,
        @NotBlank @Pattern(regexp = "[A-Za-z][A-Za-z0-9_]{0,79}") String screen,
        @NotNull Instant timestamp,
        @NotBlank @Pattern(regexp = "android") String platform,
        @NotNull @Size(max = 16) Map<String, String> properties) {
}
