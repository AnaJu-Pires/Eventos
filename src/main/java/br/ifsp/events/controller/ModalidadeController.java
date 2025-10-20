package br.ifsp.events.controller;

import br.ifsp.events.dto.modalidade.ModalidadePatchRequestDTO;
import br.ifsp.events.dto.modalidade.ModalidadeRequestDTO;
import br.ifsp.events.dto.modalidade.ModalidadeResponseDTO;
import br.ifsp.events.service.ModalidadeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/modalidades")
@Tag(name = "Modalidades", description = "Endpoints para gerenciamento de modalidades esportivas")
public class ModalidadeController {

    @Autowired
    private ModalidadeService modalidadeService;

    @Operation(summary = "Lista todas as modalidades", description = "Retorna uma lista de todas as modalidades cadastradas.")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR_EVENTOS')")
    public ResponseEntity<List<ModalidadeResponseDTO>> listarModalidades() {
        return ResponseEntity.ok(modalidadeService.findAll());
    }

    @Operation(summary = "Busca modalidade por ID", description = "Retorna os detalhes de uma modalidade específica pelo seu ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Modalidade encontrada"),
            @ApiResponse(responseCode = "404", description = "Modalidade não encontrada")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR_EVENTOS')")
    public ResponseEntity<ModalidadeResponseDTO> buscarModalidadePorId(@PathVariable Long id) {
        return ResponseEntity.ok(modalidadeService.findById(id));
    }

    @Operation(summary = "Cria uma nova modalidade", description = "Cadastra uma nova modalidade no sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Modalidade criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "409", description = "Modalidade com este nome já existe")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR_EVENTOS')")
    public ResponseEntity<ModalidadeResponseDTO> criarModalidade(@Valid @RequestBody ModalidadeRequestDTO requestDTO) {
        ModalidadeResponseDTO novaModalidade = modalidadeService.create(requestDTO);
        return new ResponseEntity<>(novaModalidade, HttpStatus.CREATED);
    }

    @Operation(summary = "Atualiza uma modalidade", description = "Atualiza todos os dados de uma modalidade existente.")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR_EVENTOS')")
    public ResponseEntity<ModalidadeResponseDTO> atualizarModalidade(@PathVariable Long id, @Valid @RequestBody ModalidadeRequestDTO requestDTO) {
        return ResponseEntity.ok(modalidadeService.update(id, requestDTO));
    }

    @Operation(summary = "Atualiza parcialmente uma modalidade", description = "Atualiza um ou mais campos de uma modalidade existente.")
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR_EVENTOS')")
    public ResponseEntity<ModalidadeResponseDTO> atualizarParcialmenteModalidade(@PathVariable Long id, @RequestBody ModalidadePatchRequestDTO requestDTO) {
        return ResponseEntity.ok(modalidadeService.patch(id, requestDTO));
    }

    @Operation(summary = "Exclui uma modalidade", description = "Remove uma modalidade do sistema, se não estiver associada a nenhum evento.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Modalidade excluída com sucesso"),
            @ApiResponse(responseCode = "400", description = "Violação de regra de negócio (ex: modalidade em uso)"),
            @ApiResponse(responseCode = "404", description = "Modalidade não encontrada")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR_EVENTOS')")
    public ResponseEntity<Void> deletarModalidade(@PathVariable Long id) {
        modalidadeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}