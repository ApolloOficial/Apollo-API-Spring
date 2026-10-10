package org.apollo.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.PhoneChangeRequestDTO;
import org.apollo.api.service.PhoneChangeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/phone-change-requests")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-key")
public class PhoneChangeRequestController {

    private final PhoneChangeService phoneChangeService;

    @GetMapping
    @Operation(summary = "Pending phone change requests of the manager's branch")
    public List<PhoneChangeRequestDTO> pending() {
        return phoneChangeService.listPending();
    }

    @PatchMapping("/{id}/approve")
    @Operation(summary = "Approve a phone change request and update the employee phone")
    public PhoneChangeRequestDTO approve(@PathVariable UUID id) {
        return phoneChangeService.approve(id);
    }

    @PatchMapping("/{id}/reject")
    @Operation(summary = "Reject a phone change request")
    public PhoneChangeRequestDTO reject(@PathVariable UUID id) {
        return phoneChangeService.reject(id);
    }
}
