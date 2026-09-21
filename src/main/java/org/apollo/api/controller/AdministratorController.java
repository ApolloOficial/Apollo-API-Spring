package org.apollo.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apollo.api.exception.ErrorResponse;
import org.apollo.api.dto.AdministratorCreateDTO;
import org.apollo.api.dto.AdministratorDTO;
import org.apollo.api.service.AdministratorService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "User management operations")
@SecurityRequirement(name = "bearer-key")
public class AdministratorController {

    private final AdministratorService administratorService;

    @GetMapping
    @Operation(summary = "List users")
    @ApiResponse(responseCode = "200", description = "Users returned successfully")
    public List<AdministratorDTO> findAll() {
        return administratorService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Find user by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User returned successfully"),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\": 404, \"message\": \"Usuário não encontrado: 1\"}")))
    })
    public AdministratorDTO findById(@PathVariable Long id) {
        return administratorService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create user")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid user data",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\": 400, \"message\": \"email: Email inválido\"}")))
    })
    public AdministratorDTO create(@Valid @RequestBody AdministratorCreateDTO dto) {
        return administratorService.create(dto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid user data",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\": 400, \"message\": \"email: Email inválido\"}"))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\": 404, \"message\": \"Usuário não encontrado: 1\"}")))
    })
    public AdministratorDTO update(@PathVariable Long id, @Valid @RequestBody AdministratorDTO dto) {
        return administratorService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete user")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\": 404, \"message\": \"Usuário não encontrado: 1\"}")))
    })
    public void delete(@PathVariable Long id) {
        administratorService.delete(id);
    }
}
