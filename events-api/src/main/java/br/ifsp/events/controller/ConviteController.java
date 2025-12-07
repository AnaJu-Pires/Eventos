package br.ifsp.events.controller;

import br.ifsp.events.dto.convite.ConviteResponseDTO;
import br.ifsp.events.dto.ErrorResponse;
import br.ifsp.events.service.ConviteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/convites")
@Tag(name = "Convites", description = "Endpoints para gerenciamento de convites de times")
@SecurityRequirement(name = "bearerAuth")
public class ConviteController {

    private final ConviteService conviteService;

    public ConviteController(ConviteService conviteService) {
        this.conviteService = conviteService;
    }

    @Operation(summary = "Lista os convites pendentes do usuário logado",
               description = "Retorna todos os convites pendentes que foram enviados para o usuário autenticado.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de convites retornada com sucesso"),
        @ApiResponse(responseCode = "403", description = "Acesso negado. Token não fornecido ou inválido")
    })
    @GetMapping("/meus-convites")
    public ResponseEntity<List<ConviteResponseDTO>> getMeusConvites(Authentication authentication) {
        return ResponseEntity.ok(conviteService.listarMeusConvites(authentication));
    }

    @Operation(summary = "Aceita um convite para entrar em um time",
               description = "O usuário logado aceita um convite pendente. O usuário é adicionado como membro do time.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Convite aceito com sucesso"),
        @ApiResponse(responseCode = "400", description = "Regra de negócio violada (ex: convite expirado, já está em um time desta modalidade)",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Acesso negado. Token não fornecido ou inválido"),
        @ApiResponse(responseCode = "404", description = "Convite não encontrado ou não pertence a este usuário",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{conviteId}/aceitar")
    public ResponseEntity<Void> aceitarConvite(
            @Parameter(description = "ID do convite a ser aceito") @PathVariable Long conviteId,
            Authentication authentication) {
        conviteService.aceitarConvite(conviteId, authentication);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Recusa um convite para entrar em um time",
               description = "O usuário logado recusa um convite pendente. O convite é removido.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Convite recusado e removido com sucesso"),
        @ApiResponse(responseCode = "403", description = "Acesso negado. Token não fornecido ou inválido"),
        @ApiResponse(responseCode = "404", description = "Convite não encontrado ou não pertence a este usuário",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{conviteId}/recusar")
    public ResponseEntity<Void> recusarConvite(
            @Parameter(description = "ID do convite a ser recusado") @PathVariable Long conviteId,
            Authentication authentication) {
        conviteService.recusarConvite(conviteId, authentication);
        return ResponseEntity.noContent().build();
    }
}