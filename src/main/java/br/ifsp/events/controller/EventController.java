package br.ifsp.events.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
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
import br.ifsp.events.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @Operation(summary = "Cria um novo evento", description = "Cadastra um novo evento no sistema.")
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

    @Operation(summary = "Atualiza um evento", description = "Atualiza todos os dados de um evento existente.")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('GESTOR_EVENTOS')")
    public ResponseEntity<EventResponseDTO> update(@PathVariable Long id, @RequestBody @Valid EventRequestDTO eventRequestDTO) {
        EventResponseDTO updatedEvent = eventService.update(id, eventRequestDTO);
        return ResponseEntity.ok(updatedEvent);
    }

    @Operation(summary = "Atualiza parcialmente um evento", description = "Atualiza um ou mais campos de um evento existente.")
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('GESTOR_EVENTOS')")
    public ResponseEntity<EventResponseDTO> patch(@PathVariable Long id, @RequestBody EventPatchDTO eventPatchDTO) {
        EventResponseDTO patchedEvent = eventService.patch(id, eventPatchDTO);
        return ResponseEntity.ok(patchedEvent);
    }

    @Operation(summary = "Exclui um evento", description = "Remove um evento do sistema.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('GESTOR_EVENTOS')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        eventService.delete(id);
        return ResponseEntity.noContent().build();
    }
}