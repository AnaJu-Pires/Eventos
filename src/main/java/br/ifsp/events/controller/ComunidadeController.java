package br.ifsp.events.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.ifsp.events.dto.comunidade.ComunidadeCreateDTO;
import br.ifsp.events.dto.comunidade.ComunidadeResponseDTO;
import br.ifsp.events.service.ComunidadeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;

import java.net.URI;

@RestController
@RequestMapping("/comunidades")
public class ComunidadeController {

    private final ComunidadeService comunidadeService;

    public ComunidadeController(ComunidadeService comunidadeService) {
        this.comunidadeService = comunidadeService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cria uma nova comunidade", description= "Cria uma nova comunidade no sistema. Requer Nível GOLD e 1500 pontos.")
    public ResponseEntity<ComunidadeResponseDTO> create(@RequestBody @Valid ComunidadeCreateDTO dto) {
        ComunidadeResponseDTO novaComunidade = comunidadeService.create(dto);

        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(novaComunidade.getId())
            .toUri();

        return ResponseEntity.created(location).body(novaComunidade);
    }
}
