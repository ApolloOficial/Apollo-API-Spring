package org.apollo.api.tracking.controller;

import jakarta.validation.Valid;
import org.apollo.api.tracking.dto.TrackingBatchRequest;
import org.apollo.api.tracking.dto.TrackingBatchResponse;
import org.apollo.api.tracking.service.TrackingService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tracking/events")
public class TrackingController {
    private final TrackingService service;

    public TrackingController(TrackingService service) { this.service = service; }

    @PostMapping("/batch")
    public TrackingBatchResponse batch(@Valid @RequestBody TrackingBatchRequest body) {
        return service.accept(body);
    }
}