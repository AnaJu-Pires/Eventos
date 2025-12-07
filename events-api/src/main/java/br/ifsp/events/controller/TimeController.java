package br.ifsp.events.controller;

import br.ifsp.events.dto.time.CapitaoTransferDTO;
import br.ifsp.events.dto.time.TimeCreateDTO;
import br.ifsp.events.dto.time.TimeUpdateDTO;
import br.ifsp.events.dto.time.TimeResponseDTO;
import br.ifsp.events.dto.ErrorResponse;
import br.ifsp.events.dto.convite.ConviteCreateDTO;
import br.ifsp.events.service.TimeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


@RestController
@RequestMapping("/times")
@Tag(name = "Times", description = "Endpoints para Gerenciamento de Times")
@SecurityRequirement(name = "bearerAuth")
public class TimeController {

    private final TimeService timeService;

    public TimeController(TimeService timeService) {
        this.timeService = timeService;
    }

    @Operation(summary = "Cria um novo time", description = "Cria um novo time com o usuário autenticado como capitão.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Time criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos (ex: modalidadeId não existe)",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Acesso negado. Token não fornecido ou inválido")
    })
    @PostMapping
    public ResponseEntity<TimeResponseDTO> createTime(
            @RequestBody @Valid TimeCreateDTO createDTO,
            Authentication authentication) {
        
        TimeResponseDTO newTime = timeService.createTime(createDTO, authentication);
        return new ResponseEntity<>(newTime, HttpStatus.CREATED);
    }

    @Operation(summary = "Busca um time por ID", description = "Retorna os detalhes de um time específico.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Time encontrado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Time não encontrado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Acesso negado. Token não fornecido ou inválido")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TimeResponseDTO> getTimeById(
            @Parameter(description = "ID do time a ser buscado") @PathVariable Long id) {
        
        return ResponseEntity.ok(timeService.getTimeById(id));
    }

    @Operation(summary = "Atualiza o nome de um time", description = "Permite que o capitão atualize o nome do time.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Nome do time atualizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos (ex: nome em branco)",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Acesso negado. Apenas o capitão do time pode realizar esta ação."),
        @ApiResponse(responseCode = "404", description = "Time não encontrado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}")
    public ResponseEntity<TimeResponseDTO> updateTime(
            @Parameter(description = "ID do time a ser atualizado") @PathVariable Long id,
            @RequestBody @Valid TimeUpdateDTO updateDTO,
            Authentication authentication) {

        TimeResponseDTO updatedTime = timeService.updateTime(id, updateDTO, authentication);
        return ResponseEntity.ok(updatedTime);
    }

    @Operation(summary = "Transfere a capitania do time", description = "Permite que o capitão atual transfira a capitania para outro usuário.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Capitania transferida com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos (ex: novo capitão é o capitão atual)",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Acesso negado. Apenas o capitão do time pode realizar esta ação."),
        @ApiResponse(responseCode = "404", description = "Time ou novo capitão não encontrado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}/capitao")
    public ResponseEntity<TimeResponseDTO> transferCaptaincy(
            @Parameter(description = "ID do time") @PathVariable Long id,
            @RequestBody @Valid CapitaoTransferDTO transferDTO,
            Authentication authentication) {

        TimeResponseDTO updatedTime = timeService.transferCaptaincy(id, transferDTO, authentication);
        return ResponseEntity.ok(updatedTime);
    }

    @Operation(summary = "Exclui um time", description = "Permite que o capitão exclua o time permanentemente.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Time excluído com sucesso"),
        @ApiResponse(responseCode = "403", description = "Acesso negado. Apenas o capitão do time pode realizar esta ação."),
        @ApiResponse(responseCode = "404", description = "Time não encontrado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTime(
            @Parameter(description = "ID do time a ser excluído") @PathVariable Long id,
            Authentication authentication) {

        timeService.deleteTime(id, authentication);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Lista todos os times", description = "Retorna uma lista paginada de todos os times cadastrados.")
    @ApiResponse(responseCode = "200", description = "Lista de times retornada com sucesso")
    @PageableAsQueryParam
    @GetMapping
    public ResponseEntity<Page<TimeResponseDTO>> listAllTimes(
            @Parameter(hidden = true) Pageable pageable) {
        
        return ResponseEntity.ok(timeService.listAllTimes(pageable));
    }

    @Operation(summary = "Convida um usuário para um time",
               description = "O capitão do time envia um convite para o e-mail de um usuário.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Convite enviado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Regra de negócio violada (ex: usuário já é membro, convite já pendente)",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Acesso negado. Apenas o capitão do time pode realizar esta ação."),
        @ApiResponse(responseCode = "404", description = "Time ou usuário a ser convidado não encontrado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{timeId}/convites")
    public ResponseEntity<Void> convidarMembro(
            @Parameter(description = "ID do time que está convidando") @PathVariable Long timeId,
            @RequestBody @Valid ConviteCreateDTO createDTO,
            Authentication authentication) {
        
        timeService.convidarMembro(timeId, createDTO, authentication);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Operation(summary = "Remove um membro de um time",
               description = "O capitão do time remove um usuário da lista de membros.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Membro removido com sucesso"),
        @ApiResponse(responseCode = "400", description = "Regra de negócio violada (ex: capitão tentando se remover)",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Acesso negado. Apenas o capitão do time pode realizar esta ação."),
        @ApiResponse(responseCode = "404", description = "Time ou membro não encontrado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{timeId}/membros/{userId}")
    public ResponseEntity<Void> removerMembro(
            @Parameter(description = "ID do time") @PathVariable Long timeId,
            @Parameter(description = "ID do usuário a ser removido") @PathVariable Long userId,
            Authentication authentication) {
        
        timeService.removerMembro(timeId, userId, authentication);
        return ResponseEntity.noContent().build();
    }
}