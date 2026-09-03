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
import org.apollo.api.dto.RolesDTO;
import org.apollo.api.service.RolesService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@Tag(name = "Roles", description = "Role management operations")
@SecurityRequirement(name = "bearer-key")
public class RolesController {

    private final RolesService rolesService;

    @GetMapping
    @Operation(summary = "List roles")
    @ApiResponse(responseCode = "200", description = "Roles returned successfully")
    public List<RolesDTO> findAll() {
        return rolesService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Find role by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Role returned successfully"),
            @ApiResponse(responseCode = "404", description = "Role not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\": 404, \"message\": \"Perfil não encontrado: 1\"}")))
    })
    public RolesDTO findById(@PathVariable Long id) {
        return rolesService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create role")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Role created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid role data",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\": 400, \"message\": \"name: Nome é obrigatório\"}")))
    })
    public RolesDTO create(@Valid @RequestBody RolesDTO dto) {
        return rolesService.create(dto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update role")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Role updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid role data",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\": 400, \"message\": \"name: Nome é obrigatório\"}"))),
            @ApiResponse(responseCode = "404", description = "Role not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\": 404, \"message\": \"Perfil não encontrado: 1\"}")))
    })
    public RolesDTO update(@PathVariable Long id, @Valid @RequestBody RolesDTO dto) {
        return rolesService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete role")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Role deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Role not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\": 404, \"message\": \"Perfil não encontrado: 1\"}")))
    })
    public void delete(@PathVariable Long id) {
        rolesService.delete(id);
    }
}
