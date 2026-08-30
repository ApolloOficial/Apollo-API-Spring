package org.apollo.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apollo.api.exception.ErrorResponse;
import org.apollo.api.dto.PanelDTO;
import org.apollo.api.service.PanelService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/panels")
@RequiredArgsConstructor
@Tag(name = "Panel", description = "Panel management operations")
public class PanelController {

    private final PanelService panelService;

    @GetMapping
    @Operation(summary = "Listar painéis")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Painéis retornados com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\": 401, \"message\": \"Token ausente ou inválido\"}")))
    })
    public List<PanelDTO> findAll() {
        return panelService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar painel por identificador")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Painel retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\": 401, \"message\": \"Token ausente ou inválido\"}"))),
            @ApiResponse(responseCode = "404", description = "Painel não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\": 404, \"message\": \"Painel não encontrado: 1\"}")))
    })
    public PanelDTO findById(@PathVariable Long id) {
        return panelService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Criar painel")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Painel criado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\": 401, \"message\": \"Token ausente ou inválido\"}")))
    })
    public PanelDTO create(@Valid @RequestBody PanelDTO dto) {
        return panelService.create(dto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar painel")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Painel atualizado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\": 401, \"message\": \"Token ausente ou inválido\"}"))),
            @ApiResponse(responseCode = "404", description = "Painel não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\": 404, \"message\": \"Painel não encontrado: 1\"}")))
    })
    public PanelDTO update(@PathVariable Long id, @Valid @RequestBody PanelDTO dto) {
        return panelService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir painel")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Painel excluído com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\": 401, \"message\": \"Token ausente ou inválido\"}"))),
            @ApiResponse(responseCode = "404", description = "Painel não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\": 404, \"message\": \"Painel não encontrado: 1\"}")))
    })
    public void delete(@PathVariable Long id) {
        panelService.delete(id);
    }
}
