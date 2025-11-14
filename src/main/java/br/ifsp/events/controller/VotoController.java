package br.ifsp.events.controller;

import br.ifsp.events.dto.voto.VotoCreateDTO;
import br.ifsp.events.dto.voto.VotoResponseDTO;
import br.ifsp.events.service.VotoService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/votos")
public class VotoController {

    private final VotoService votoService;

    public VotoController(VotoService votoService) {
        this.votoService = votoService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Registra um voto (upvote/downvote)",
               description = "Registra, atualiza ou remove um voto em um post ou comentário. Retorna o novo placar e o voto atual do usuário.")
    public ResponseEntity<VotoResponseDTO> votar(@RequestBody @Valid VotoCreateDTO dto) {

        VotoResponseDTO response = votoService.votar(dto);

        return ResponseEntity.ok(response);
    }
    
}
