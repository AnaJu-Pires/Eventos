package br.ifsp.events.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.ifsp.events.dto.inscricao.InscricaoResponseDTO;
import br.ifsp.events.service.InscricaoService;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/inscricoes")
public class InscricaoController {

    private final InscricaoService inscricaoService;

    public InscricaoController(InscricaoService inscricaoService) {
        this.inscricaoService = inscricaoService;
    }

    @Operation(summary = "Aprova uma inscrição", description = "Muda o status de uma inscrição para 'APROVADA'.")
    @PostMapping("/{inscricaoId}/aprovar")
    @PreAuthorize("hasRole('GESTOR_EVENTOS')")
    public ResponseEntity<InscricaoResponseDTO> aprovarInscricao(@PathVariable Long inscricaoId) {
        InscricaoResponseDTO inscricao = inscricaoService.aprovarInscricao(inscricaoId);
        return ResponseEntity.ok(inscricao);
    }

    @Operation(summary = "Rejeita uma inscrição", description = "Muda o status de uma inscrição para 'REJEITADA'.")
    @PostMapping("/{inscricaoId}/rejeitar")
    @PreAuthorize("hasRole('GESTOR_EVENTOS')")
    public ResponseEntity<InscricaoResponseDTO> rejeitarInscricao(@PathVariable Long inscricaoId) {
        InscricaoResponseDTO inscricao = inscricaoService.rejeitarInscricao(inscricaoId);
        return ResponseEntity.ok(inscricao);
    }
}