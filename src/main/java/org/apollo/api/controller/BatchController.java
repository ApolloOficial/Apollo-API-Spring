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
import org.apollo.api.dto.BatchDTO;
import org.apollo.api.service.BatchService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/batches")
@RequiredArgsConstructor
@Tag(name = "Lotes", description = "Operações de gerenciamento de lotes")
@SecurityRequirement(name = "bearer-key")
public class BatchController {

    private final BatchService batchService;

    @GetMapping
    @Operation(summary = "Listar lotes")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lotes retornados com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\": 401, \"message\": \"Token ausente ou inválido\"}")))
    })
    public List<BatchDTO> findAll() {
        return batchService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar lote por identificador")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lote retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\": 401, \"message\": \"Token ausente ou inválido\"}"))),
            @ApiResponse(responseCode = "404", description = "Lote não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\": 404, \"message\": \"Lote não encontrado: 1\"}")))
    })
    public BatchDTO findById(@PathVariable Long id) {
        return batchService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Criar lote")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lote criado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\": 401, \"message\": \"Token ausente ou inválido\"}")))
    })
    public BatchDTO create(@Valid @RequestBody BatchDTO dto) {
        return batchService.create(dto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar lote")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lote atualizado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\": 401, \"message\": \"Token ausente ou inválido\"}"))),
            @ApiResponse(responseCode = "404", description = "Lote não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\": 404, \"message\": \"Lote não encontrado: 1\"}")))
    })
    public BatchDTO update(@PathVariable Long id, @Valid @RequestBody BatchDTO dto) {
        return batchService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir lote")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lote excluído com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\": 401, \"message\": \"Token ausente ou inválido\"}"))),
            @ApiResponse(responseCode = "404", description = "Lote não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\": 404, \"message\": \"Lote não encontrado: 1\"}")))
    })
    public void delete(@PathVariable Long id) {
        batchService.delete(id);
    }
}
