package br.ifsp.events.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.ifsp.events.dto.event.EventPatchDTO;
import br.ifsp.events.dto.event.EventRequestDTO;
import br.ifsp.events.dto.event.EventResponseDTO;
import br.ifsp.events.dto.event.GerarChaveRequestDTO;
import br.ifsp.events.dto.inscricao.InscricaoRequestDTO;
import br.ifsp.events.dto.inscricao.InscricaoResponseDTO;
import br.ifsp.events.dto.partida.PartidaResponseDTO;
import br.ifsp.events.service.EventService;
import br.ifsp.events.service.InscricaoService;
import br.ifsp.events.service.PartidaService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;
    // ADICIONADOS:
    private final InscricaoService inscricaoService;
    private final PartidaService partidaService;

    // CONSTRUTOR ATUALIZADO:
    public EventController(EventService eventService, InscricaoService inscricaoService, PartidaService partidaService) {
        this.eventService = eventService;
        this.inscricaoService = inscricaoService;
        this.partidaService = partidaService;
    }

    @Operation(summary = "Cria um novo evento", description = "Cadastra um novo evento no sistema.") // <-- Operation adicionada
    @PostMapping
    @PreAuthorize("hasRole('GESTOR_EVENTOS')")
    public ResponseEntity<EventResponseDTO> create(@RequestBody @Valid EventRequestDTO eventRequestDTO) {
        EventResponseDTO createdEvent = eventService.create(eventRequestDTO);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdEvent.getId())
                .toUri();

        return ResponseEntity.created(location).body(createdEvent);
    }

    @Operation(summary = "Atualiza um evento", description = "Atualiza todos os dados de um evento existente.") // <-- Operation adicionada
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('GESTOR_EVENTOS')")
    public ResponseEntity<EventResponseDTO> update(@PathVariable Long id, @RequestBody @Valid EventRequestDTO eventRequestDTO) {
        EventResponseDTO updatedEvent = eventService.update(id, eventRequestDTO);
        return ResponseEntity.ok(updatedEvent);
    }

    @Operation(summary = "Atualiza parcialmente um evento", description = "Atualiza um ou mais campos de um evento existente.") // <-- Operation adicionada
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('GESTOR_EVENTOS')")
    public ResponseEntity<EventResponseDTO> patch(@PathVariable Long id, @RequestBody EventPatchDTO eventPatchDTO) {
        EventResponseDTO patchedEvent = eventService.patch(id, eventPatchDTO);
        return ResponseEntity.ok(patchedEvent);
    }

    @Operation(summary = "Exclui um evento", description = "Remove um evento do sistema.") // <-- Operation adicionada
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('GESTOR_EVENTOS')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        eventService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ===================================
    // MÉTODOS ADICIONADOS
    // ===================================

    @Operation(summary = "Inscreve um time em um evento", description = "Inscreve o time do capitão autenticado em um evento.")
    @PostMapping("/{id}/inscrever")
    public ResponseEntity<InscricaoResponseDTO> inscreverTime(@PathVariable("id") Long eventoId, @RequestBody @Valid InscricaoRequestDTO requestDTO) {
        InscricaoResponseDTO inscricao = eventService.inscreverTime(eventoId, requestDTO.getTimeId());
        return ResponseEntity.status(HttpStatus.CREATED).body(inscricao);
    }

    @Operation(summary = "Lista inscrições pendentes", description = "Lista todas as inscrições pendentes de um evento.")
    @GetMapping("/{id}/inscricoes")
    @PreAuthorize("hasRole('GESTOR_EVENTOS')")
    public ResponseEntity<List<InscricaoResponseDTO>> getInscricoesPendentes(@PathVariable Long id) {
        List<InscricaoResponseDTO> inscricoes = inscricaoService.listPendentesByEvento(id);
        return ResponseEntity.ok(inscricoes);
    }

    @Operation(summary = "Lista partidas de um evento", description = "Retorna todas as partidas de um evento, com times e placares. Este endpoint é público.")
    @GetMapping("/{id}/partidas")
    public ResponseEntity<List<PartidaResponseDTO>> getPartidasDoEvento(@PathVariable Long id) {
        List<PartidaResponseDTO> partidas = partidaService.listByEvento(id);
        return ResponseEntity.ok(partidas);
    }

    @Operation(summary = "Lista todos os eventos", description = "Retorna todos os eventos (sem partidas).")
    @GetMapping
    public ResponseEntity<java.util.List<EventResponseDTO>> getAllEvents() {
        java.util.List<EventResponseDTO> events = eventService.listAll();
        return ResponseEntity.ok(events);
    }

    @Operation(summary = "Detalha um evento", description = "Retorna um evento com seus dados e partidas associadas.")
    @GetMapping("/{id}")
    public ResponseEntity<EventResponseDTO> getEventWithPartidas(@PathVariable Long id) {
        EventResponseDTO event = eventService.findById(id);
        // carregar partidas do evento
        java.util.List<PartidaResponseDTO> partidas = partidaService.listByEvento(id);
        event.setPartidas(partidas);
        return ResponseEntity.ok(event);
    }

    @Operation(summary = "Atualiza resultado de uma partida", description = "Atualiza os placares de uma partida e define vencedor/status. Requer permissões de gestor.")
    @PatchMapping("/{eventoId}/partidas/{partidaId}/resultado")
    @PreAuthorize("hasRole('GESTOR_EVENTOS')")
    public ResponseEntity<Void> atualizarResultado(@PathVariable Long eventoId, @PathVariable Long partidaId,
            @RequestBody @Valid br.ifsp.events.dto.partida.PartidaResultadoRequestDTO request) {
        partidaService.atualizarResultado(eventoId, partidaId, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Gera chave de confrontos (ex.: mata-mata ou pontos corridos)", 
               description = "Gera automaticamente a chave de confrontos para as modalidades do evento no formato solicitado. Requer permissões de gestor e que as inscrições estejam fechadas.")
    @PostMapping("/{id}/gerar-chave")
    @PreAuthorize("hasRole('GESTOR_EVENTOS')")
    public ResponseEntity<Void> gerarChave(@PathVariable("id") Long eventoId, @RequestBody @Valid GerarChaveRequestDTO request) {
        partidaService.gerarChaveParaEvento(eventoId, request.getFormato());
        return ResponseEntity.noContent().build();
    }
}