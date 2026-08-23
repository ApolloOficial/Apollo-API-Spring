package org.apollo.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.LoginRequestDTO;
import org.apollo.api.dto.LoginResponseDTO;
import org.apollo.api.service.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Operações de autenticação")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Autenticar usuário e emitir token JWT")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token JWT emitido com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de login inválidos"),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas")
    })
    public LoginResponseDTO login(@Valid @RequestBody LoginRequestDTO request) {
        return authService.login(request);
    }
}
