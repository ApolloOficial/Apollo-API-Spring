package org.apollo.api.tracking.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.UUID;

public record TrackingBatchResponse(
        @JsonProperty("accepted_event_ids") List<UUID> acceptedEventIds) {
}