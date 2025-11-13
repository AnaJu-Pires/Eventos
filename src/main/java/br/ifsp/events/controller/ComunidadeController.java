package br.ifsp.events.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.ifsp.events.dto.comunidade.ComunidadeCreateDTO;
import br.ifsp.events.dto.comunidade.ComunidadeResponseDTO;
import br.ifsp.events.dto.post.PostCreateDTO;
import br.ifsp.events.dto.post.PostResponseDTO;
import br.ifsp.events.service.ComunidadeService;
import br.ifsp.events.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/comunidades")
public class ComunidadeController {

    private final ComunidadeService comunidadeService;
    private final PostService postService;

    public ComunidadeController(ComunidadeService comunidadeService, PostService postService) {
        this.comunidadeService = comunidadeService;
        this.postService = postService;
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

    @PostMapping("/{comunidadeId}/posts")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cria um novo post em uma comunidade", description = "Cria um novo post. Requer 100 pontos de saldo.")
    public ResponseEntity<PostResponseDTO> createPost(
            @PathVariable Long comunidadeId, 
            @RequestBody @Valid PostCreateDTO dto) {
        
        PostResponseDTO novoPost = postService.create(dto, comunidadeId);

        return ResponseEntity.status(201).body(novoPost);
    }
}
