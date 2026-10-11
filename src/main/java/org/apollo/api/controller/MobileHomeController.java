package org.apollo.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.MobileHomeDTO;
import org.apollo.api.service.MobileHomeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/mobile/home")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-key")
public class MobileHomeController {

    private final MobileHomeService mobileHomeService;

    @GetMapping
    @Operation(summary = "Mobile home: header, open service orders, attention items and pending panel registrations")
    public MobileHomeDTO home() {
        return mobileHomeService.home();
    }
}
