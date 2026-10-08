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
import org.apollo.api.dto.MeDTO;
import org.apollo.api.exception.ErrorResponse;
import org.apollo.api.security.TenantContext;
import org.apollo.api.service.AuthService;
import org.apollo.api.service.ProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-key")
public class AuthController {

    private final AuthService authService;
    private final ProfileService profileService;
    private final TenantContext tenantContext;

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and issue JWT token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "JWT token issued successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\": 401, \"message\": \"Invalid credentials\"}")))
    })
    public LoginResponseDTO login(@Valid @RequestBody LoginRequestDTO request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    @Operation(summary = "Data of the logged user (name, role, company and branch) for the app header")
    public MeDTO me() {
        return profileService.me();
    }

    @PatchMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Change own password")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Password changed successfully"),
            @ApiResponse(responseCode = "400", description = "Current password incorrect or new password invalid",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\": 400, \"message\": \"Senha atual incorreta\"}")))
    })
    public void changePassword(@Valid @RequestBody ChangePasswordDTO dto) {
        authService.changePassword(tenantContext.getUserId(), tenantContext.getCompanyId(), dto);
    }
}