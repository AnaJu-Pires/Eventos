package br.ifsp.events.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.ifsp.events.dto.comentario.ComentarioCreateDTO;
import br.ifsp.events.dto.comentario.ComentarioResponseDTO;
import br.ifsp.events.dto.post.PostResponseDTO;
import br.ifsp.events.service.ComentarioService;
import br.ifsp.events.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/posts")
public class PostController {

    private final ComentarioService comentarioService;
    private final PostService postService;

    public PostController(ComentarioService comentarioService, PostService postService) {
        this.comentarioService = comentarioService;
        this.postService = postService;
    }

    @PostMapping("/{postId}/comentarios")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cria um novo comentário em um post", 
               description = "Cria um novo comentário ou uma resposta a outro comentário. Recompensa o autor com 5 pontos.")
    public ResponseEntity<ComentarioResponseDTO> createComentario(
            @PathVariable Long postId, 
            @RequestBody @Valid ComentarioCreateDTO dto) {
        
        ComentarioResponseDTO novoComentario = comentarioService.create(dto, postId);

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/comentarios/{id}")
                .buildAndExpand(novoComentario.getId())
                .toUri();

        return ResponseEntity.created(location).body(novoComentario);
    }

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Busca um post por ID", 
               description = "Retorna os dados de um post específico, incluindo seu placar de votos atual.")
    public ResponseEntity<PostResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(postService.findById(id));
    }
}