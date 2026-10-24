package org.apollo.api.sungrow;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/integrations/sungrow")
@ConditionalOnSungrowLive
@Tag(name = "Sungrow", description = "Autorizacao da integracao com a API aberta da Sungrow")
@SecurityRequirement(name = "bearer-key")
public class SungrowAuthorizationController {
    private static final Logger log = LoggerFactory.getLogger(SungrowAuthorizationController.class);

    private final SungrowTokenService tokens;

    public SungrowAuthorizationController(SungrowTokenService tokens) {
        this.tokens = tokens;
    }

    public record AuthorizationRequest(@NotBlank String code) {
    }

    public record AuthorizationStatus(boolean authorized, Instant accessTokenExpiresAt, String authorizedPlants) {
    }

    @PostMapping("/authorization")
    @Operation(summary = "Exchange the authorization code returned by the Sungrow redirect for tokens")
    public AuthorizationStatus authorize(@Valid @RequestBody AuthorizationRequest request) {
        try {
            return status(tokens.authorize(request.code().trim()));
        } catch (SungrowApiException exception) {
            log.warn("Sungrow authorization failed: {}", exception.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Sungrow rejected the authorization code (" + exception.getCode() + ")");
        }
    }

    @GetMapping("/status")
    @Operation(summary = "Tell whether the Sungrow integration is authorized (never returns the tokens)")
    public AuthorizationStatus status() {
        return tokens.current().map(SungrowAuthorizationController::status)
                .orElse(new AuthorizationStatus(false, null, null));
    }

    private static AuthorizationStatus status(SungrowTokens tokens) {
        return new AuthorizationStatus(true, tokens.expiresAt(), tokens.authorizedPlants());
    }
}
