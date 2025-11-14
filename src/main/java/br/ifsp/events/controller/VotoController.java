package br.ifsp.events.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.ifsp.events.dto.ErrorResponse;
import br.ifsp.events.dto.voto.VotoCreateDTO;
import br.ifsp.events.dto.voto.VotoResponseDTO;
import br.ifsp.events.service.VotoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/votos")
@Tag(name = "Votos", description = "Endpoints para votar em posts e comentários")
@SecurityRequirement(name = "bearerAuth")
public class VotoController {

    private final VotoService votoService;

    public VotoController(VotoService votoService) {
        this.votoService = votoService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Registra um voto (upvote/downvote)",
               description = "Registra, atualiza ou remove um voto em um post ou comentário. Retorna o novo placar e o voto atual do usuário.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Voto processado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos (ex: votar em post e comentário ao mesmo tempo)",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "404", description = "Post ou Comentário não encontrado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<VotoResponseDTO> votar(@RequestBody @Valid VotoCreateDTO dto) {

        VotoResponseDTO response = votoService.votar(dto);

        return ResponseEntity.ok(response);
    }
    
}
