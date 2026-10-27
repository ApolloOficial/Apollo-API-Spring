package org.apollo.api.sungrow;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.Optional;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/integrations/sungrow")
@Tag(name = "Sungrow", description = "Autorizacao da integracao com a API aberta da Sungrow")
@SecurityRequirement(name = "bearer-key")
public class SungrowStatusController {
    private final SungrowProperties properties;
    private final ObjectProvider<SungrowTokenService> tokens;

    public SungrowStatusController(SungrowProperties properties, ObjectProvider<SungrowTokenService> tokens) {
        this.properties = properties;
        this.tokens = tokens;
    }

    public record Status(boolean enabled, String mode, boolean authorized, Instant accessTokenExpiresAt,
                         String authorizedPlants) {
    }

    @GetMapping("/status")
    @Operation(summary = "Tell how the Sungrow integration runs (DISABLED, SIMULATED or LIVE) and, in LIVE, whether it is authorized (never returns the tokens)")
    public Status status() {
        if (!properties.isEnabled()) {
            return new Status(false, "DISABLED", false, null, null);
        }
        if (properties.isSimulated()) {
            return new Status(true, "SIMULATED", false, null, null);
        }
        Optional<SungrowTokens> current = Optional.ofNullable(tokens.getIfAvailable())
                .flatMap(SungrowTokenService::current);
        return current.map(value -> new Status(true, "LIVE", true, value.expiresAt(), value.authorizedPlants()))
                .orElse(new Status(true, "LIVE", false, null, null));
    }
}
