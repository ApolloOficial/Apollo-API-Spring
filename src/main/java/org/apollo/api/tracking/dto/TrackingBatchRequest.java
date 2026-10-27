package org.apollo.api.tracking.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record TrackingBatchRequest(
        @NotEmpty @Size(max = 50) List<@NotNull @Valid TrackingEventRequest> events) {
}