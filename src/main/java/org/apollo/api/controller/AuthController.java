package org.apollo.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.ChangePasswordDTO;
import org.apollo.api.dto.LoginRequestDTO;
import org.apollo.api.dto.LoginResponseDTO;
import org.apollo.api.exception.ErrorResponse;
import org.apollo.api.security.TenantContext;
import org.apollo.api.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-key")
public class AuthController {

    private final AuthService authService;
    private final TenantContext tenantContext;

    @PostMapping("/login")
    @Operation(summary = "Autenticar usuário e emitir token JWT")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token JWT emitido com sucesso"),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\": 401, \"message\": \"Credenciais inválidas\"}")))
    })
    public LoginResponseDTO login(@Valid @RequestBody LoginRequestDTO request) {
        return authService.login(request);
    }

    @PatchMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Trocar a própria senha")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Senha alterada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Senha atual incorreta ou nova senha inválida",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\": 400, \"message\": \"Senha atual incorreta\"}")))
    })
    public void changePassword(@Valid @RequestBody ChangePasswordDTO dto) {
        authService.changePassword(tenantContext.getUserId(), tenantContext.getCompanyId(), dto);
    }
}